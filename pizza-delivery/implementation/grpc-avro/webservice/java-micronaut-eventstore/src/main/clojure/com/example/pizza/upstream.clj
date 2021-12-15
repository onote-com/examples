(ns com.example.pizza.upstream
  (:require [jsonista.core :as json])
  (:import [com.eventstore.dbclient
            EventStoreDBClient
            EventStoreDBConnectionString
            EventData
            EventDataBuilder
            EventStoreDBProjectionManagementClient]))

(comment

  (def order-schema
    [:map
     ["id" {} :uuid]
     ["line-items"
      {}
      [:vector
       [:map
        ["item-id" {} :uuid]
        ["quantity" {} :int]
        ["price" {} :int]
        ["notes" {} :string]]]]
     ["subtotal" {} :int]
     ["tax" {} :int]
     ["total" {} :int]
     ["customer-id" {} :uuid]
     ["delivery-address"
      {}
      [:map
       ["address1" {} :string]
       ["address2" {} :string]
       ["city" {} :string]
       ["state" {} :string]
       ["zip" {} :string]]]
     ["type" {} [:ref "74ccf771-f64d-4518-ba5c-80d6e2acae5c"]]
     ["status" {} [:ref "762f93ef-8a51-4c17-a8c1-bf850a222e0c"]]])

  (def settings
    (EventStoreDBConnectionString/parseOrThrow
     "esdb://localhost:2113?tls=false"))

  (def db-client
    (EventStoreDBClient/create settings))

  (let [order-id (java.util.UUID/randomUUID)
        order    {"id"              order-id
                  "lineItems"       [{"itemId"   (java.util.UUID/randomUUID)
                                      "quantity" 2
                                      "price"    500
                                      "notes"    "extra cheese"}]
                  "subtotal"        1000
                  "tax"             100
                  "total"           1100
                  "customerId"      (java.util.UUID/randomUUID)
                  "deliveryAddress" {"address1" "123 Main Street"
                                     "address2" ""
                                     "city"     "Anytown"
                                     "state"    "NY"
                                     "zip"      "10011"}
                  "type"            "DELIVERY"}
        event    (.build
                  (EventDataBuilder/json
                   "OrderPlaced"
                   ^bytes
                   (json/write-value-as-bytes order)))]
    @(.appendToStream db-client
                      (str "orders-" order-id)
                      (into-array EventData [event])))

  (def result *1)

  (.getLogPosition result)
  (.getNextExpectedRevision result)

  (def projection-client
    (EventStoreDBProjectionManagementClient/create settings))

  @(.getResult projection-client
               "fulfillment-status"
               java.util.HashMap)

  (count (get *1 "orders"))

  ;;
  )
