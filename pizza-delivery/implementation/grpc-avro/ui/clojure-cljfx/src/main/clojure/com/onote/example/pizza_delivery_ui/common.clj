(ns com.onote.example.pizza-delivery-ui.common
  (:require  [clojure.core.cache :as cache]
             [io.pedestal.log :as log]
             [cljfx.api :as fx]
             [com.onote.example.pizza-delivery-ui.client :as client]))

(defn make-context
  ([]
   (make-context {}))
  ([initial-state]
   (atom
    (fx/create-context
     initial-state
     cache/lru-cache-factory))))

(defmulti client-effect
  (fn [client v _dispatch!] (:client/method v))
  :default ::default)

(defmethod client-effect ::default
  [client request _]
  (log/warn ::client-effect ::default :request request))

(defmethod client-effect :orders-to-fulfill
  [client req dispatch!]
  (try
    (let [response (client/orders-to-fulfill client req)]
      (dispatch! {:event/type :orders-to-fulfill-fetched
                  :response   response}))
    (catch Exception e
      (log/error :exception e)
      (dispatch! {:event/type :client-error
                  :request    req
                  :message    "failed to fetch orders to fulfill"
                  :exception  e}))))

(defmethod client-effect :change-order-status
  [client {:keys [order-id status] :as req} dispatch!]
  (try
    (if (client/change-order-status client order-id status)
      (dispatch! {:event/type :order-status-changed
                  :order-id   order-id
                  :status     status})
      (dispatch! {:event/type :order-status-change-failed
                  :order-id   order-id
                  :status     status}))
    (catch Exception e
      (log/error :exception e)
      (dispatch! {:event/type :client-error
                  :request    req
                  :message
                  (format "failed to change order %s to status %s"
                          order-id status)
                  :exception  e}))))

(defn make-client-effect
  [client]
  (partial client-effect client))

(defn make-app
  [{:keys [context event-handler root-view client] :as _config}]
  (fx/create-app
   context
   :event-handler event-handler
   :effects {:client (make-client-effect client)}
   :desc-fn (fn [_] {:fx/type root-view})))

(defn stop-app
  [{:keys [context renderer] :as _app}]
  (fx/unmount-renderer context renderer))
