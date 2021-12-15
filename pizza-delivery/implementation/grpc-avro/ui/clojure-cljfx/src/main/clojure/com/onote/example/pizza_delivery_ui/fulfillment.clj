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

(defmethod -event-handler ::change-order-status
  [{:keys [order-id status]}]
  {:client {:client/method :change-order-status
            :success-event ::order-status-changed
            :order-id      order-id
            :status        status}})

(defmethod -event-handler ::order-status-changed
  [_]
  {:dispatch-later {:ms       200
                    :dispatch {:event/type ::fetch-read-model}}})

(defn event-handler
  [event]
  (log/debug ::event-handler event)
  (-event-handler event))

(defn orders-sub
  [context]
  (fx/sub-val context :orders-to-fulfill))

(def prev-status
  {"PREPARING" "PLACED"
   "IN_OVEN"   "PREPARING"
   "EN_ROUTE"  "IN_OVEN"})

(def next-status
  {"PLACED"    "PREPARING"
   "PREPARING" "IN_OVEN"
   "IN_OVEN"   "EN_ROUTE"
   "EN_ROUTE"  "DELIVERED"})

(defn orders-list
  [{:keys [fx/context]}]
  (let [orders (fx/sub-ctx context orders-sub)]
    {:fx/type  :v-box
     :children (for [{:keys [id status] :as order} orders]
                 {:fx/type  :h-box
                  :padding  {:top 10 :bottom 10}
                  :children [{:fx/type     :label
                              :h-box/hgrow :always
                              :padding     {:left 10 :right 10}
                              :text        (str id)}
                             (if-let [prev (prev-status status)]
                               {:fx/type   :button
                                :padding   {:left 10 :right 10}
                                :text      (str "Mark as " prev)
                                :on-action {:event/type ::change-order-status
                                            :status     prev
                                            :order-id   id}}
                               {:fx/type :label
                                :padding {:left 10 :right 10}
                                :text    "Newly PLACED"})
                             (if-let [nxt (next-status status)]
                               {:fx/type   :button
                                :padding   {:left 10 :right 10}
                                :on-action {:event/type ::change-order-status
                                            :status     nxt
                                            :order-id   id}
                                :text      (str "Mark as " nxt)}
                               {:fx/type :label
                                :padding {:left 10 :right 10}
                                :text    "DELIVERED"})]})}))

(defn root-view
  [_]
  {:fx/type :stage
   :title   "Pizza Order Fulfillment"
   :showing true
   :width   600
   :height  400
   :scene
   {:fx/type :scene
    :root
    {:fx/type  :v-box
     :children [{:fx/type   :h-box
                 :alignment :center
                 :padding   {:top    10
                             :bottom 10}
                 :children  [{:fx/type :label
                              :padding {:right 30}
                              :style   {:-fx-font-size 24}
                              :text    "Orders to Fulfill"}
                             {:fx/type   :button
                              :text      "Refresh"
                              :on-action {:event/type ::fetch-read-model}}]}
                {:fx/type orders-list}]}}})
