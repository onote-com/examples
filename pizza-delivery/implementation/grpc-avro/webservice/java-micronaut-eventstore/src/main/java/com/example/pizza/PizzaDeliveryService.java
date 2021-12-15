package com.example.pizza;

import com.eventstore.dbclient.EventData;
import com.eventstore.dbclient.EventStoreDBClient;
import com.eventstore.dbclient.EventStoreDBProjectionManagementClient;
import com.eventstore.dbclient.WriteResult;
import com.example.PizzaDelivery;
import com.example.pizza.command.MarkOrderRequest;
import com.example.pizza.event.OrderStatusChanged;
import com.example.pizza.event.OrderStatusChangedStatusEnum;
import com.example.pizza.read_model.OrdersToFulfillRequest;
import com.example.pizza.read_model.OrdersToFulfillResponse;
import io.micronaut.context.annotation.Property;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

@Singleton
public class PizzaDeliveryService implements PizzaDelivery {
    private static final Logger logger = LoggerFactory.getLogger(PizzaDeliveryService.class);

    private final EventStoreDBClient dbClient;
    private final EventStoreDBProjectionManagementClient projectionClient;
    private final String fulfillmentStreamCategory;
    private final String fulfillmentStatusProjection;

    public PizzaDeliveryService(EventStoreDBClient client,
                                EventStoreDBProjectionManagementClient projectionClient,
                                @Property(name = "event-store.stream-categories.fulfillment")
                                        String fulfillmentStreamCategory,
                                @Property(name = "event-store.projections.fulfillment-status")
                                        String fulfillmentStatusProjection) {
        this.projectionClient = projectionClient;
        this.dbClient = client;
        this.fulfillmentStreamCategory = fulfillmentStreamCategory;
        this.fulfillmentStatusProjection = fulfillmentStatusProjection;
    }

    @Override
    public OrdersToFulfillResponse ordersToFulfill(OrdersToFulfillRequest request) {
        logger.info("ordersToFulfill: {}", request);
        OrdersToFulfillResponse.Builder responseBuilder = OrdersToFulfillResponse.newBuilder();
        try {
            FulfillmentStatusProjectionDTO fulfillmentStatus = projectionClient
                    .getResult(fulfillmentStatusProjection, FulfillmentStatusProjectionDTO.class)
                    .get();
            logger.info("Fetched fulfillment-status projection: {}", fulfillmentStatus.getOrders());
            responseBuilder.setOrders(new ArrayList<>(fulfillmentStatus.getOrders().values()));
        } catch (Throwable e) {
            logger.error("Error while fetching fulfillment-status projection:", e);
        }
        return responseBuilder.build();
    }

    @Override
    public boolean changeOrderStatus(MarkOrderRequest request) {
        logger.info("changeOrderStatus: {}", request);

        OrderStatusChanged orderStatusChanged = OrderStatusChanged.newBuilder()
                .setOrderId(request.getOrderId())
                .setStatus(OrderStatusChangedStatusEnum.valueOf(request.getStatus().name()))
                .build();

        EventData event = AvroEventDataBuilder
                .json("OrderStatusChanged", orderStatusChanged)
                .build();

        try {
            WriteResult result = dbClient.appendToStream(
                    fulfillmentStreamCategory + "-" + request.getOrderId(),
                    event
            ).get();
            logger.info("Appended event: {} to stream with result: {}", event, result);
            return true;
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error while writing to the fulfillment stream", e);
            return false;
        }
    }
}
