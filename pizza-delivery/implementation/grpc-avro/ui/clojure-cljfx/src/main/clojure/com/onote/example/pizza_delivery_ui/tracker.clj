(ns com.onote.example.pizza-delivery-ui.tracker
  (:require [cljfx.api :as fx]
            [io.pedestal.log :as log]))

(set! *warn-on-reflection* true)

(def initial-state {})

(defmulti -event-handler :event/type
  :default ::default)

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
  {:context (fx/swap-context
             context assoc
             :orders-to-fulfill (:orders response))})

(defn event-handler
  [event]
  (log/debug ::event-handler event)
  (-event-handler event))

(defn header-view
  [_]
  {:fx/type   :h-box
   :alignment :center
   :children  [{:fx/type :label
                :text    "Pizza"}]})

(defn orders-sub
  [context]
  (fx/sub-val context :orders-to-fulfill))

(defn get-order-sub
  [context order-id]
  (when-let [orders (fx/sub-val context :orders-to-fulfill)]
    (some (fn [{:keys [id]}] (= id order-id))
          orders)))

(defn current-order-id-sub
  [context]
  (fx/sub-val context :order-id))

(defn current-order-status-sub
  [context]
  (let [order-id (fx/sub-ctx context current-order-id-sub)]
    (:status (fx/sub-ctx get-order-sub order-id))))

(defn root-view
  [{:keys [fx/context]}]
  (let [order-id (fx/sub-ctx context current-order-id-sub)
        status   (fx/sub-ctx context current-order-status-sub)]
    {:fx/type :stage
     :title   "Pizza Tracker"
     :showing true
     :width   600
     :height  400
     :scene
     {:fx/type :scene
      :root
      {:fx/type   :v-box
       :alignment :center
       :children  [{:fx/type   :h-box
                    :alignment :center
                    :children
                    [{:fx/type :label
                      :text    "Order ID"}
                     {:fx/type        :text-field
                      :text-formatter {:fx/type          :text-formatter
                                       :value-converter  :default
                                       :value            (str order-id)
                                       :on-value-changed {:event/type ::set-order-id}}}]}
                   {:fx/type   :h-box
                    :alignment :center
                    :children
                    [{:fx/type :label
                      :text    "Status"}
                     {:fx/type :label
                      :text    status}]}]}}}))
