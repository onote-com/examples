package com.onote.example.grpc;

import com.example.PizzaDelivery;
import com.example.pizza.MarkOrderRequest;
import com.example.pizza.OrdersToFulfillRequest;
import com.example.pizza.OrdersToFulfillResponse;
import jakarta.inject.Singleton;

@Singleton
public class PizzaDeliveryService implements PizzaDelivery {

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
