(ns com.onote.example.pizza-delivery-ui.fulfillment
  (:require [cljfx.api :as fx]
            [io.pedestal.log :as log]))

(def initial-state {})

(defmulti -event-handler :event/type :default ::default)

(defmethod -event-handler ::default
  [event]
  (log/warn :unhandled-event event))

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
