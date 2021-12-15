(ns com.onote.example.pizza-delivery-ui.client
  (:require [io.pedestal.log :as log])
  (:import [io.grpc ManagedChannelBuilder]
           [org.apache.avro.grpc AvroGrpcClient]
           [com.example PizzaDelivery]
           [com.example.pizza
            Order
            OrderStatus]
           [com.example.pizza.command
            MarkOrderRequest]
           [com.example.pizza.read_model
            OrdersToFulfillRequest
            OrdersToFulfillResponse]))

(set! *warn-on-reflection* true)

;; TODO: use Callback/async client

;; TODO
(defn delivery-address-map
  [delivery-address]
  {})

;; TODO
(defn line-item-map
  [line-item]
  {})

(defn order-map
  [^Order order]
  {:id               (str (.getId order))
   :line-items       (map line-item-map (.getLineItems order))
   :subtotal         (.getSubtotal order)
   :tax              (.getTax order)
   :total            (.getTotal order)
   :customer-id      (str (.getCustomerId order))
   :delivery-address (some-> order .getDeliveryAddress delivery-address-map)
   :type             (some-> order .getType .name)
   :status           (some-> order .getStatus .name)})

(defn orders-to-fulfill
  [^PizzaDelivery client _params]
  (let [^OrdersToFulfillResponse
        response (.ordersToFulfill client (.build (OrdersToFulfillRequest/newBuilder)))
        orders   (mapv order-map (.getOrders response))]
    (log/info ::orders-to-fulfill orders)
    {:orders orders}))

(defn ^boolean change-order-status
  [^PizzaDelivery client order-id status]
  (let [response (.changeOrderStatus
                  client
                  (MarkOrderRequest. (str order-id)
                                     (OrderStatus/valueOf status)))]
    (log/info ::change-order-status response)
    response))

(defn make-client
  [{:keys [^String host port]}]
  (try
    (some->
     (ManagedChannelBuilder/forAddress host (int port))
     .usePlaintext
     .build
     (AvroGrpcClient/create PizzaDelivery))
    (catch Throwable t
      (log/error :exception t))))

(defmulti client-effect
  (fn [_client v _dispatch!] (:client/method v))
  :default ::default)

(defmethod client-effect ::default
  [_client request _]
  (log/warn ::client-effect ::default :request request))

(defmethod client-effect :orders-to-fulfill
  [client
   {:keys [success-event error-event] :as req}
   dispatch!]
  (try
    (let [response (orders-to-fulfill client req)]
      (dispatch! {:event/type (or success-event :orders-to-fulfill-fetched)
                  :response   response}))
    (catch Exception e
      (log/error :exception e)
      (dispatch! {:event/type (or error-event :client-error)
                  :request    req
                  :message    "failed to fetch orders to fulfill"
                  :exception  e}))))

(defmethod client-effect :change-order-status
  [client
   {:keys [order-id status success-event error-event] :as req}
   dispatch!]
  (try
    (if (change-order-status client order-id status)
      (dispatch! {:event/type (or success-event :order-status-changed)
                  :order-id   order-id
                  :status     status})
      (dispatch! {:event/type (or error-event :order-status-change-failed)
                  :order-id   order-id
                  :status     status}))
    (catch Exception e
      (log/error :exception e)
      (dispatch! {:event/type (or error-event :client-error)
                  :request    req
                  :message
                  (format "failed to change order %s to status %s"
                          order-id status)
                  :exception  e}))))

(defn make-effect
  [client]
  (partial client-effect client))

(comment

  (def client (:grpc/client integrant.repl.state/system))
  (orders-to-fulfill client nil)
  (change-order-status client #uuid "b52b0717-3ec5-4987-b7a8-4e7d8aa66db9" "EN_ROUTE"))
