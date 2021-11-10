package com.onote.example.grpc;

import io.grpc.ManagedChannel;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.grpc.annotation.GrpcChannel;
import io.micronaut.grpc.server.GrpcServerChannel;
import org.apache.avro.grpc.AvroGrpcClient;
// import com.onote.examples.grpc.TestModel;

@Factory
class Clients {
    // @Bean
    // TestModel testModelClient(@GrpcChannel(GrpcServerChannel.NAME) ManagedChannel channel) {
    //     return AvroGrpcClient.create(channel, TestModel.class);
    // }
}
