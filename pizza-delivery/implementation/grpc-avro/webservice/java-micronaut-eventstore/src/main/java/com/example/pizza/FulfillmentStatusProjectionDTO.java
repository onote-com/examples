package com.example.pizza;

import java.util.HashMap;

public class FulfillmentStatusProjectionDTO {
    private HashMap<String, Order> orders;

    public HashMap<String, Order> getOrders() {
        return orders;
    }

    public void setOrders(HashMap<String, Order> orders) {
        this.orders = orders;
    }
}
