(ns com.onote.example.pizza-delivery-ui.grpc
  (:require [clojure.java.io :as io])
  (:import [org.apache.avro Protocol]
           [io.grpc ManagedChannelBuilder]
           [org.apache.avro.grpc AvroGrpcClient]
           [com.example.pizza PizzaDeliveryTracker]))

(defn make-client
  [{:keys [^String host port]}]
  (let [client
        (some->
         (ManagedChannelBuilder/forAddress host (int port))
         .usePlaintext
         .build
         (AvroGrpcClient/create PizzaDeliveryTracker))]
    client))
