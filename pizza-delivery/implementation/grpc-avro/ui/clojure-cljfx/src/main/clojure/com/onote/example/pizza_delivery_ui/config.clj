(ns com.onote.example.pizza-delivery-ui.config
  (:require [clojure.java.io :as io]
            [integrant.core :as ig]
            [com.walmartlabs.dyn-edn :as dyn-edn]
            [io.pedestal.log :as log]
            [com.onote.example.pizza-delivery-ui.client :as client]
            [com.onote.example.pizza-delivery-ui.common :as common]
            [com.onote.example.pizza-delivery-ui.fulfillment :as fulfillment]))

(set! *warn-on-reflection* true)

(defn config
  []
  (->> "config.edn"
       io/resource
       slurp
       (ig/read-string {:readers (dyn-edn/env-readers)})))

(defmethod ig/init-key :env/env
  [_ env]
  env)

(defmethod ig/init-key :env/development?
  [_ env]
  (= env :dev))

;; TODO: create w/ different initial-state depending on which app is passed/configured
(defmethod ig/init-key :cljfx/context
  [_ _]
  (log/info :cljfx/context :initialize)
  (common/make-context fulfillment/initial-state))

;; TODO: create different app depending on which app is passed/configured
(defmethod ig/init-key :cljfx/app
  [_ config]
  (log/info :cljfx/app :init)
  (merge config (fulfillment/make-app config)))

(defmethod ig/halt-key! :cljfx/app
  [_ app]
  (log/info :cljfx/app :halt!)
  (common/stop-app app))

(defmethod ig/init-key :grpc/client
  [_ config]
  (log/info :grpc/client :init)
  (client/make-client config))
