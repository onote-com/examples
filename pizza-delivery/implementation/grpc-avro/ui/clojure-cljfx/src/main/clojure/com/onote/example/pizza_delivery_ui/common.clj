(ns com.onote.example.pizza-delivery-ui.common
  (:require  [clojure.core.cache :as cache]
             [cljfx.api :as fx]
             [com.onote.example.pizza-delivery-ui.client :as client]))

(set! *warn-on-reflection* true)

(defn make-context
  ([]
   (make-context {}))
  ([initial-state]
   (atom
    (fx/create-context
     initial-state
     cache/lru-cache-factory))))

(defn make-app
  [{:keys [context event-handler root-view client] :as _config}]
  (fx/create-app
   context
   :event-handler event-handler
   :effects {:client (client/make-effect client)}
   :desc-fn (fn [_] {:fx/type root-view})))

(defn stop-app
  [{:keys [context renderer] :as _app}]
  (fx/unmount-renderer context renderer))
