package com.example.pizza;

import com.example.PizzaDelivery;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

@Singleton
public class PizzaDeliveryService implements PizzaDelivery {
    private static final Logger logger = LoggerFactory.getLogger(PizzaDeliveryService.class);

    @Override
    public OrdersToFulfillResponse ordersToFulfill(OrdersToFulfillRequest request) {
        // Lookup data from whatever materialized view store
        logger.info("ordersToFulfill: {}", request);
        OrdersToFulfillResponse.Builder builder = OrdersToFulfillResponse
                .newBuilder()
                .setOrders(new ArrayList<>());
        return builder.build();
    }

    @Override
    public boolean changeOrderStatus(MarkOrderRequest request) {
        // Write event to event log
        logger.info("changeOrderStatus: {}", request);
        return false;
    }
}
