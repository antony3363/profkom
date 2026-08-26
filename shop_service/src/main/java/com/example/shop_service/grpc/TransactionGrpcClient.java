package com.example.shop_service.grpc;

import com.example.shop_service.exceptions.InsufficientStockException;
import com.example.transactions_service.grpc.ChargePurchaseRequest;
import com.example.transactions_service.grpc.RefundPurchaseRequest;
import com.example.transactions_service.grpc.TransactionGrpcServiceGrpc;
import com.example.transactions_service.grpc.TransactionInfo;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TransactionGrpcClient {

    private final TransactionGrpcServiceGrpc.TransactionGrpcServiceBlockingStub transactionGrpcServiceBlockingStub;

    /**
     * @return id созданной транзакции списания
     * @throws InsufficientStockException переиспользуется как общая "покупка не прошла"
     *         ошибка — семантически это скорее InsufficientBalance, но со стороны
     *         Shop Service единственное, что видно снаружи — "покупку выполнить не
     *         удалось", поэтому один и тот же 409-статус.
     */
    public UUID chargePurchase(long buyerUserId, long amount, UUID purchaseId, String description) {
        try {
            TransactionInfo info = transactionGrpcServiceBlockingStub.chargePurchase(ChargePurchaseRequest.newBuilder()
                    .setBuyerUserId(buyerUserId)
                    .setAmount(amount)
                    .setPurchaseId(purchaseId.toString())
                    .setDescription(description)
                    .build());
            return UUID.fromString(info.getTransactionId());
        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == Status.Code.FAILED_PRECONDITION) {
                throw new InsufficientStockException("Не удалось списать баллы за покупку " + purchaseId + ": " + e.getStatus().getDescription());
            }
            throw new IllegalStateException("Transactions Service недоступен: " + e.getStatus(), e);
        }
    }

    public void refundPurchase(long buyerUserId, long amount, UUID purchaseId, String description) {
        transactionGrpcServiceBlockingStub.refundPurchase(RefundPurchaseRequest.newBuilder()
                .setBuyerUserId(buyerUserId)
                .setAmount(amount)
                .setPurchaseId(purchaseId.toString())
                .setDescription(description)
                .build());
    }
}
