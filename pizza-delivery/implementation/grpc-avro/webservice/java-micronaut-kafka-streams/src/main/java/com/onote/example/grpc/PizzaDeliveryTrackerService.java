package com.onote.example.grpc;

import com.example.pizza.*;
import jakarta.inject.Singleton;

@Singleton
public class PizzaDeliveryTrackerService implements PizzaDeliveryTracker {

    @Override
    public OrdersToFulfillResponse ordersToFulfill(OrdersToFulfillRequest request) {
        // Lookup data from whatever materialized view store
        return null;
    }

    @Override
    public boolean changeOrderStatus(MarkOrderRequest request) {
        // Write event to event log
        return false;
    }
}
