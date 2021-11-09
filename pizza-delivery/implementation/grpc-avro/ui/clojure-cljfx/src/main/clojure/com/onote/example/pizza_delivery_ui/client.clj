(ns com.onote.example.pizza-delivery-ui.client
  (:import [io.grpc ManagedChannelBuilder]
           [org.apache.avro.grpc AvroGrpcClient]
           [com.example.pizza
            MarkOrderRequest
            Order
            OrdersToFulfillRequest
            OrdersToFulfillResponse
            PizzaDeliveryTracker]))

(set! *warn-on-reflection* true)

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
  {:id               (java.util.UUID/fromString (.getId order))
   :line-items       (map line-item-map (.getLineItems order))
   :subtotal         (.getSubtotal order)
   :tax              (.getTax order)
   :total            (.getTotal order)
   :customer-id      (java.util.UUID/fromString (.getCustomerId order))
   :delivery-address (some-> order .getDeliveryAddress delivery-address-map)
   :type             (.getType order)   ;; TODO: type enum to keyword
   :status           (.getStatus order) ;; TODO: status enum to keyword
   })

(defn orders-to-fulfill
  [^PizzaDeliveryTracker client _params]
  (let [^OrdersToFulfillResponse orders (.ordersToFulfill client (OrdersToFulfillRequest.))]
    {:orders (into [] (map order-map (.getOrders orders)))}))

(defn ^boolean change-order-status
  [^PizzaDeliveryTracker client order-id status]
  ;; TODO: status enum from keyword
  (.changeOrderStatus client (MarkOrderRequest. order-id status)))

(defn make-client
  [{:keys [^String host port]}]
  (let [client
        (some->
         (ManagedChannelBuilder/forAddress host (int port))
         .usePlaintext
         .build
         (AvroGrpcClient/create PizzaDeliveryTracker))]
    client))
