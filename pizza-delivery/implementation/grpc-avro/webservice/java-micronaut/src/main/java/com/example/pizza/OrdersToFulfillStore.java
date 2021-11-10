package com.example.pizza;

import jakarta.inject.Singleton;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Singleton
public class OrdersToFulfillStore {
    private final ConcurrentMap<String, Order> store = new ConcurrentHashMap<>();

    public List<Order> all() {
        return new ArrayList<>(store.values());
    }

    public void put(String id, Order order) {
        store.put(id, order);
    }

    public Order get(String id) {
        return store.get(id);
    }
}
