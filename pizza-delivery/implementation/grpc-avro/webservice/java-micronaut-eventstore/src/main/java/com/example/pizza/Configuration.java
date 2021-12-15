package com.example.pizza;

import com.eventstore.dbclient.EventStoreDBClient;
import com.eventstore.dbclient.EventStoreDBClientSettings;
import com.eventstore.dbclient.EventStoreDBConnectionString;
import com.eventstore.dbclient.EventStoreDBProjectionManagementClient;
import com.example.PizzaDelivery;
import io.grpc.BindableService;
import io.grpc.ServerServiceDefinition;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Property;
import org.apache.avro.grpc.AvroGrpcServer;

@Factory
public class Configuration {
    @Bean
    BindableService pizzaService(PizzaDelivery service) {
        return new BindableService() {
            @Override
            public ServerServiceDefinition bindService() {
                return AvroGrpcServer.createServiceDefinition(PizzaDelivery.class, service);
            }
        };
    }

    @Bean
    EventStoreDBClientSettings eventStoreDBClientSettings(@Property(name = "event-store.url")
                                                          final String url) {
        return EventStoreDBConnectionString.parseOrThrow(url);
    }

    @Bean
    EventStoreDBClient eventStoreDBClient(EventStoreDBClientSettings settings) {
        return EventStoreDBClient.create(settings);
    }

    @Bean
    EventStoreDBProjectionManagementClient
    eventStoreDBProjectionManagementClient(EventStoreDBClientSettings settings) {
        return EventStoreDBProjectionManagementClient.create(settings);
    }
}
