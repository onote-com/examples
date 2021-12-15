fromCategory("orders")
    .when({
        OrderPlaced: function(state, event) {
            event.body.status = "PLACED";
            emit(
                "fulfillment-" + event.body.id,
                event.eventType,
                event.body,
                event.metadata || {}
            );
        }
    });
