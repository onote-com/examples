(ns ^:no-doc user
  (:require [clojure.repl :refer :all]
            [clojure.pprint :refer [pprint]]
            [clojure.tools.namespace.repl :refer [refresh set-refresh-dirs]]
            [clojure.spec.test.alpha :as stest]
            [meta-merge.core :refer [meta-merge]]
            [integrant.core :as ig]
            [integrant.repl :refer [clear go halt suspend resume prep init reset reset-all]]
            [integrant.repl.state :refer [system]]
            [eftest.runner :as eftest]
            [com.onote.example.pizza-delivery-ui.config :as config]
            [clojure.java.browse :refer [browse-url]]))

(set! *warn-on-reflection* true)

(def dev-config
  {:cljfx/app {:app :fulfillment}})

(ns-unmap *ns* 'test)

(defn test []
  (eftest/run-tests (eftest/find-tests "test") {:multithread? false}))

(defn instrument
  []
  (stest/instrument))

(defn unstrument
  []
  (stest/unstrument))

(defn dev
  []
  (require 'com.onote.example.pizza-delivery-ui.config)
  (integrant.repl/set-prep! #(-> ((resolve 'com.onote.example.pizza-delivery-ui.config/config))
                                 (meta-merge dev-config))))

(defn re-render
  []
  (when-let [renderer (some-> system :cljfx/app :renderer)]
    (renderer)))
