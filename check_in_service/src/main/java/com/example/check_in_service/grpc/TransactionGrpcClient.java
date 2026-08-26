package com.example.check_in_service.grpc;

import com.example.transactions_service.grpc.AwardEventRewardRequest;
import com.example.transactions_service.grpc.TransactionGrpcServiceGrpc;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TransactionGrpcClient {

    private final TransactionGrpcServiceGrpc.TransactionGrpcServiceBlockingStub transactionGrpcServiceBlockingStub;

    public void awardEventReward(long reviewerUserId, long receiverUserId, long amount, UUID eventId) {
        transactionGrpcServiceBlockingStub.awardEventReward(AwardEventRewardRequest.newBuilder()
                .setReviewerUserId(reviewerUserId)
                .setReceiverUserId(receiverUserId)
                .setAmount(amount)
                .setEventId(eventId.toString())
                .setDescription("Посещение мероприятия")
                .build());
    }
}
