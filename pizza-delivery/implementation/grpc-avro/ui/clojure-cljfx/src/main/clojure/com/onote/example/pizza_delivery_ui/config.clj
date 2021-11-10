(ns com.onote.example.pizza-delivery-ui.config
  (:require [clojure.java.io :as io]
            [integrant.core :as ig]
            [com.walmartlabs.dyn-edn :as dyn-edn]
            [io.pedestal.log :as log]
            [com.onote.example.pizza-delivery-ui.client :as client]
            [com.onote.example.pizza-delivery-ui.common :as common]
            [com.onote.example.pizza-delivery-ui.fulfillment :as fulfillment]
            [com.onote.example.pizza-delivery-ui.tracker :as tracker]))

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

(defmethod ig/init-key :cljfx/app
  [_ {:keys [app] :as config}]
  (log/info :cljfx/app :init)
  (let [context (case app
                  :fulfillment (common/make-context fulfillment/initial-state)
                  :tracker     (common/make-context tracker/initial-state))
        {:keys [handler] :as application}
        (merge config
               {:context context}
               (common/make-app
                (merge config
                       {:context context}
                       (case app
                         :fulfillment {:event-handler fulfillment/event-handler
                                       :root-view     fulfillment/root-view}
                         :tracker     {:event-handler tracker/event-handler
                                       :root-view     tracker/root-view}))))]
    (when (fn? handler)
      (log/info :app/init :dispatch)
      (handler {:event/type :app/init}))
    application))

(defmethod ig/halt-key! :cljfx/app
  [_ app]
  (log/info :cljfx/app :halt!)
  (common/stop-app app))

(defmethod ig/init-key :grpc/client
  [_ config]
  (log/info :grpc/client :init)
  (client/make-client config))
