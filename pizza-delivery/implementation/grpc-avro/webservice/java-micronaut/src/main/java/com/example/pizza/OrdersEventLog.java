package com.example.pizza;

import jakarta.inject.Singleton;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Singleton
public class OrdersEventLog {
    private BlockingQueue<Order> queue = new LinkedBlockingQueue<>();

    public void put(Order order) {
        try {
            queue.put(order);
        } catch (InterruptedException e) {
            // TODO: logging
            e.printStackTrace();
        }
    }

    public Order take() {
        try {
            return queue.take();
        } catch (InterruptedException e) {
            // TODO: logging
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
