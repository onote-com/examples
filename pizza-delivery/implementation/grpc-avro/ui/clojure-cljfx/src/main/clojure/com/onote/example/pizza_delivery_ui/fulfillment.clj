(ns com.onote.example.pizza-delivery-ui.fulfillment
  (:require [cljfx.api :as fx]
            [io.pedestal.log :as log]))

(set! *warn-on-reflection* true)

(def initial-state {})

(defmulti -event-handler :event/type :default ::default)

(defmethod -event-handler ::default
  [event]
  (log/warn :unhandled-event event))

(defmethod -event-handler :app/init
  [e]
  (log/info :app/init e)
  {:dispatch {:event/type ::fetch-read-model}})

(defmethod -event-handler ::fetch-read-model
  [_]
  {:client {:client/method :orders-to-fulfill
            :success-event ::read-model-fetched}})

(defmethod -event-handler ::read-model-fetched
  [{:keys [fx/context response]}]
  {:context (fx/swap-context context assoc
             :orders-to-fulfill (:orders response))})

(defn event-handler
  [event]
  (log/debug ::event-handler event)
  (-event-handler event))

(defn root-view
  [_]
  {:fx/type :stage
   :title   "Pizza Order Fulfillment"
   :showing true
   :width   600
   :height  400
   :scene   {:fx/type :scene
             :root    {:fx/type   :v-box
                       :alignment :center
                       :children  [{:fx/type :label
                                    :text    "Hello world"}]}}})
