package com.example.events_service.config;

import com.example.profile_service.grpc.PersonGrpcServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProfileServiceGrpcConfig {

    @Bean
    public ManagedChannel profileServiceGrpcChannel(
            @Value("${profile-service.grpc.host}") String host,
            @Value("${profile-service.grpc.port}") int port) {
        return ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
    }

    @Bean
    public PersonGrpcServiceGrpc.PersonGrpcServiceBlockingStub personGrpcServiceBlockingStub(ManagedChannel profileServiceGrpcChannel) {
        return PersonGrpcServiceGrpc.newBlockingStub(profileServiceGrpcChannel);
    }
}
