package com.example.check_in_service.config;

import com.example.events_service.grpc.EventGrpcServiceGrpc;
import com.example.transactions_service.grpc.TransactionGrpcServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcClientsConfig {

    @Bean
    @Qualifier("eventsServiceGrpcChannel")
    public ManagedChannel eventsServiceGrpcChannel(
            @Value("${events-service.grpc.host}") String host,
            @Value("${events-service.grpc.port}") int port) {
        return ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
    }

    @Bean
    @Qualifier("transactionsServiceGrpcChannel")
    public ManagedChannel transactionsServiceGrpcChannel(
            @Value("${transactions-service.grpc.host}") String host,
            @Value("${transactions-service.grpc.port}") int port) {
        return ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
    }

    @Bean
    public EventGrpcServiceGrpc.EventGrpcServiceBlockingStub eventGrpcServiceBlockingStub(
            @Qualifier("eventsServiceGrpcChannel") ManagedChannel channel) {
        return EventGrpcServiceGrpc.newBlockingStub(channel);
    }

    @Bean
    public TransactionGrpcServiceGrpc.TransactionGrpcServiceBlockingStub transactionGrpcServiceBlockingStub(
            @Qualifier("transactionsServiceGrpcChannel") ManagedChannel channel) {
        return TransactionGrpcServiceGrpc.newBlockingStub(channel);
    }
}
