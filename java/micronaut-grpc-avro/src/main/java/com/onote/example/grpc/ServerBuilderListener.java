package com.onote.example.grpc;

import io.grpc.ServerBuilder;
import io.micronaut.context.event.BeanCreatedEvent;
import io.micronaut.context.event.BeanCreatedEventListener;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.apache.avro.grpc.AvroGrpcServer;
// import com.onote.examples.grpc.TestModel;

@Singleton
public class ServerBuilderListener implements BeanCreatedEventListener<io.grpc.ServerBuilder<?>> {
    // @Inject
    // TestModel model;

    @Override
    public io.grpc.ServerBuilder<?> onCreated(BeanCreatedEvent<ServerBuilder<?>> event) {
        final ServerBuilder<?> builder = event.getBean();
        // builder.addService(AvroGrpcServer.createServiceDefinition(TestModel.class, model));
        return builder;
    }
}
