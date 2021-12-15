fromCategory("fulfillment")
    .when({
        $init: function() {
            return {orders: {}};
        },
        OrderPlaced: function(state, event) {
            state.orders[event.body.id] = event.body;
        },
        OrderStatusChanged: function(state, event) {
            if (event.body.status === "DELIVERED") {
                delete state.orders[event.body.orderId];
            } else {
                let order = state.orders[event.body.orderId]
                if (typeof order !== 'undefined') {
                    order.status = event.body.status;
                }
            }
        }
    })
    .outputState();
