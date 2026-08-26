package com.example.transactions_service.grpc;

import com.example.transactions_service.DTOs.EventRewardRequestDTO;
import com.example.transactions_service.DTOs.PurchaseChargeRequestDTO;
import com.example.transactions_service.DTOs.TransactionResponseDTO;
import com.example.transactions_service.exceptions.EntityNotFoundException;
import com.example.transactions_service.exceptions.InsufficientBalanceException;
import com.example.transactions_service.services.TransactionService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TransactionGrpcServer extends TransactionGrpcServiceGrpc.TransactionGrpcServiceImplBase {

    private final TransactionService transactionService;

    @Override
    public void awardEventReward(AwardEventRewardRequest request, StreamObserver<TransactionInfo> responseObserver) {
        try {
            EventRewardRequestDTO dto = EventRewardRequestDTO.builder()
                    .receiverUserId(request.getReceiverUserId())
                    .amount(request.getAmount())
                    .eventId(UUID.fromString(request.getEventId()))
                    .description(request.getDescription())
                    .build();
            TransactionResponseDTO response = transactionService.eventReward(request.getReviewerUserId(), dto);
            respond(responseObserver, response);
        } catch (Exception e) {
            responseObserver.onError(toGrpcError(e).asRuntimeException());
        }
    }

    @Override
    public void chargePurchase(ChargePurchaseRequest request, StreamObserver<TransactionInfo> responseObserver) {
        try {
            PurchaseChargeRequestDTO dto = PurchaseChargeRequestDTO.builder()
                    .buyerUserId(request.getBuyerUserId())
                    .amount(request.getAmount())
                    .purchaseId(UUID.fromString(request.getPurchaseId()))
                    .description(request.getDescription())
                    .build();
            TransactionResponseDTO response = transactionService.chargePurchase(dto);
            respond(responseObserver, response);
        } catch (Exception e) {
            responseObserver.onError(toGrpcError(e).asRuntimeException());
        }
    }

    @Override
    public void refundPurchase(RefundPurchaseRequest request, StreamObserver<TransactionInfo> responseObserver) {
        try {
            PurchaseChargeRequestDTO dto = PurchaseChargeRequestDTO.builder()
                    .buyerUserId(request.getBuyerUserId())
                    .amount(request.getAmount())
                    .purchaseId(UUID.fromString(request.getPurchaseId()))
                    .description(request.getDescription())
                    .build();
            TransactionResponseDTO response = transactionService.refundPurchase(dto);
            respond(responseObserver, response);
        } catch (Exception e) {
            responseObserver.onError(toGrpcError(e).asRuntimeException());
        }
    }

    private void respond(StreamObserver<TransactionInfo> responseObserver, TransactionResponseDTO dto) {
        responseObserver.onNext(toProto(dto));
        responseObserver.onCompleted();
    }

    private TransactionInfo toProto(TransactionResponseDTO dto) {
        TransactionInfo.Builder builder = TransactionInfo.newBuilder()
                .setTransactionId(dto.getTransactionId().toString())
                .setType(dto.getType().name())
                .setAmount(dto.getAmount())
                .setStatus(dto.getStatus().name())
                .setCreatedAt(dto.getCreatedAt().toString());
        if (dto.getSenderWalletId() != null) builder.setSenderWalletId(dto.getSenderWalletId().toString());
        if (dto.getReceiverWalletId() != null) builder.setReceiverWalletId(dto.getReceiverWalletId().toString());
        if (dto.getRelatedEventId() != null) builder.setRelatedEventId(dto.getRelatedEventId().toString());
        if (dto.getRelatedPurchaseId() != null) builder.setRelatedPurchaseId(dto.getRelatedPurchaseId().toString());
        if (dto.getDescription() != null) builder.setDescription(dto.getDescription());
        return builder.build();
    }

    private Status toGrpcError(Exception e) {
        if (e instanceof EntityNotFoundException) {
            return Status.NOT_FOUND.withDescription(e.getMessage()).withCause(e);
        }
        if (e instanceof InsufficientBalanceException) {
            return Status.FAILED_PRECONDITION.withDescription(e.getMessage()).withCause(e);
        }
        if (e instanceof IllegalArgumentException) {
            return Status.INVALID_ARGUMENT.withDescription(e.getMessage()).withCause(e);
        }
        return Status.INTERNAL.withDescription(e.getMessage()).withCause(e);
    }
}
