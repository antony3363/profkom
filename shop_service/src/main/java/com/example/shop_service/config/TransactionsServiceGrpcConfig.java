package com.example.shop_service.config;

import com.example.transactions_service.grpc.TransactionGrpcServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TransactionsServiceGrpcConfig {

    @Bean
    public ManagedChannel transactionsServiceGrpcChannel(
            @Value("${transactions-service.grpc.host}") String host,
            @Value("${transactions-service.grpc.port}") int port) {
        return ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
    }

    @Bean
    public TransactionGrpcServiceGrpc.TransactionGrpcServiceBlockingStub transactionGrpcServiceBlockingStub(ManagedChannel transactionsServiceGrpcChannel) {
        return TransactionGrpcServiceGrpc.newBlockingStub(transactionsServiceGrpcChannel);
    }
}
