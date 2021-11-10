package com.example.pizza;

import com.example.pizza.event.OrderStatusChanged;
import com.example.pizza.read_model.OrdersToFulfillResponseOrdersRecordDeliveryAddressRecord;
import com.example.pizza.read_model.OrdersToFulfillResponseOrdersRecordLineItemsRecord;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

@Singleton
public class EventProcessor implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(EventProcessor.class);

    private static final List<Order> initialOrders = Arrays.asList(
            Order.newBuilder()
                    .setId("6e120178-5ce7-417d-82bd-686fd9d5c242")
                    .setType(OrderType.DELIVERY)
                    .setStatus(OrderStatus.PREPARING)
                    .setSubtotal(950)
                    .setTax(50)
                    .setTotal(1000)
                    .setCustomerId("e50ad4a9-865d-40ae-bc9b-6b2a4c35c686")
                    .setDeliveryAddress(
                            OrdersToFulfillResponseOrdersRecordDeliveryAddressRecord.newBuilder()
                                    .setAddress1("123 Main Street")
                                    .setAddress2("")
                                    .setCity("Anytown")
                                    .setState("CA")
                                    .setZip("90210")
                                    .build()
                    )
                    .setLineItems(
                            List.of(
                                    OrdersToFulfillResponseOrdersRecordLineItemsRecord.newBuilder()
                                            .setItemId("1234")
                                            .setNotes("Extra Cheese")
                                            .setPrice(950)
                                            .setQuantity(1)
                                            .build()
                            )
                    )
                    .build(),
            Order.newBuilder()
                    .setId("b52b0717-3ec5-4987-b7a8-4e7d8aa66db9")
                    .setType(OrderType.DELIVERY)
                    .setStatus(OrderStatus.PREPARING)
                    .setSubtotal(1950)
                    .setTax(50)
                    .setTotal(2000)
                    .setCustomerId("34691372-bdfe-4b48-a528-8c647f955c2d")
                    .setDeliveryAddress(
                            OrdersToFulfillResponseOrdersRecordDeliveryAddressRecord.newBuilder()
                                    .setAddress1("321 State Street")
                                    .setAddress2("")
                                    .setCity("Anytown")
                                    .setState("NY")
                                    .setZip("10001")
                                    .build()
                    )
                    .setLineItems(
                            List.of(
                                    OrdersToFulfillResponseOrdersRecordLineItemsRecord.newBuilder()
                                            .setItemId("4321")
                                            .setNotes("No anchovies")
                                            .setPrice(1950)
                                            .setQuantity(1)
                                            .build()
                            )
                    )
                    .build()
            );

    private final OrdersEventLog ordersEventLog;
    private final AtomicBoolean processingOrders = new AtomicBoolean(false);

    private final OrderStatusChangedEventLog orderStatusChangedEventLog;
    private final AtomicBoolean processingOrderStatusChanged = new AtomicBoolean(false);

    private final OrdersToFulfillStore store;

    private final ExecutorService executor = Executors.newFixedThreadPool(2);

    public EventProcessor(OrdersEventLog ordersEventLog,
                          OrderStatusChangedEventLog orderStatusChangedEventLog,
                          OrdersToFulfillStore store) {
        this.ordersEventLog = ordersEventLog;
        this.orderStatusChangedEventLog = orderStatusChangedEventLog;
        this.store = store;
    }

    @PostConstruct
    public void initialize() {
        logger.info("Initializing EventProcessor...");

        executor.submit(new Runnable() {
            @Override
            public void run() {
                processingOrders.set(true);
                while (processingOrders.get()) {
                    Order event = ordersEventLog.take();
                    Order order = Order.newBuilder(event).build();
                    logger.info(
                            "Order event arrived: {}. Adding the order to the OrdersToFulfill read model",
                            event);
                    store.put(order.getId(), order);
                }
            }
        });

        executor.submit(new Runnable() {
            @Override
            public void run() {
                processingOrderStatusChanged.set(true);
                while (processingOrderStatusChanged.get()) {
                    OrderStatusChanged event = orderStatusChangedEventLog.take();

                    String orderId = event.getOrderId();

                    logger.info(
                            "Order Status Changed event arrived: {}. Updating order {} in OrdersToFulfill read model",
                            event,
                            orderId);

                    Order order = store.get(orderId);

                    if (order != null) {
                        order.setStatus(OrderStatus.valueOf(event.getStatus().name()));
                    }
                }
            }
        });

        for (Order order : initialOrders) {
            ordersEventLog.put(order);
        }

        logger.info("...done");
    }

    @PreDestroy
    public void close() {
        logger.info("Shutting down EventProcessor...");
        processingOrders.set(false);
        processingOrderStatusChanged.set(false);
        executor.shutdown();
        logger.info("...done");
    }
}
