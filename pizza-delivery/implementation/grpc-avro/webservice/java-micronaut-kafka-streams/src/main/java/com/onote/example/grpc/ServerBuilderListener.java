package com.onote.example.grpc;

import com.example.PizzaDelivery;

import io.grpc.ServerBuilder;
import io.micronaut.context.event.BeanCreatedEvent;
import io.micronaut.context.event.BeanCreatedEventListener;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.apache.avro.grpc.AvroGrpcServer;


@Singleton
public class ServerBuilderListener implements BeanCreatedEventListener<io.grpc.ServerBuilder<?>> {
    @Inject
    PizzaDeliveryService model;

    @Override
    public io.grpc.ServerBuilder<?> onCreated(BeanCreatedEvent<ServerBuilder<?>> event) {
        final ServerBuilder<?> builder = event.getBean();
        builder.addService(AvroGrpcServer.createServiceDefinition(PizzaDelivery.class, model));
        return builder;
    }
}
