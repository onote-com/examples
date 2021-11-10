package com.example.pizza;

import com.example.pizza.event.OrderStatusChanged;
import jakarta.inject.Singleton;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Singleton
public class OrderStatusChangedEventLog {
    private BlockingQueue<OrderStatusChanged> queue = new LinkedBlockingQueue<>();

    public void put(OrderStatusChanged event) {
        try {
            queue.put(event);
        } catch (InterruptedException e) {
            // TODO: logging
            e.printStackTrace();
        }
    }

    public OrderStatusChanged take() {
        try {
            return queue.take();
        } catch (InterruptedException e) {
            // TODO: logging
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

}
