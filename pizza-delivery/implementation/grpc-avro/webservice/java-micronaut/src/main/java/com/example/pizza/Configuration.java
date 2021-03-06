package com.example.pizza;

import com.example.PizzaDelivery;
import io.grpc.BindableService;
import io.grpc.ServerServiceDefinition;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import jakarta.inject.Inject;
import org.apache.avro.grpc.AvroGrpcServer;

@Factory
public class Configuration {
    @Inject EventProcessor processor;

    @Bean
    BindableService pizzaService(PizzaDelivery service) {
        return new BindableService() {
            @Override
            public ServerServiceDefinition bindService() {
                return AvroGrpcServer.createServiceDefinition(PizzaDelivery.class, service);
            }
        };
    }
}
