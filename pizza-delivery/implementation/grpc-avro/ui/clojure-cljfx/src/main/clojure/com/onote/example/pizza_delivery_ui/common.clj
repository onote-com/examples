(ns com.onote.example.pizza-delivery-ui.common
  (:require  [clojure.core.cache :as cache]
             [cljfx.api :as fx]))

(defn make-context
  ([]
   (make-context {}))
  ([initial-state]
   (atom
    (fx/create-context
     initial-state
     cache/lru-cache-factory))))

(defn stop-app
  [{:keys [context #_handler renderer] :as _app}]
  (fx/unmount-renderer context renderer))
