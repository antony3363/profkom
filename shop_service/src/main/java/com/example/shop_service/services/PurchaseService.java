package com.example.shop_service.services;

import com.example.shop_service.DTOs.PurchaseCreateRequestDTO;
import com.example.shop_service.DTOs.PurchaseResponseDTO;
import com.example.shop_service.entities.Product;
import com.example.shop_service.entities.ProductVariant;
import com.example.shop_service.entities.Purchase;
import com.example.shop_service.enums.PurchaseStatus;
import com.example.shop_service.exceptions.EntityNotFoundException;
import com.example.shop_service.exceptions.InsufficientStockException;
import com.example.shop_service.grpc.TransactionGrpcClient;
import com.example.shop_service.repositories.ProductVariantRepository;
import com.example.shop_service.repositories.PurchaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final ProductVariantRepository productVariantRepository;
    private final TransactionGrpcClient transactionGrpcClient;

    /**
     * Списывает сток варианта под блокировкой строки (чтобы не продать больше, чем
     * есть в наличии при параллельных покупках), затем сразу синхронно списывает
     * баллы через Transactions Service. При неудаче — сток возвращается, покупка
     * помечается CANCELLED, ошибка пробрасывается вызывающей стороне.
     */
    @Transactional
    public PurchaseResponseDTO createPurchase(long buyerId, PurchaseCreateRequestDTO dto) {
        ProductVariant variant = productVariantRepository.findWithLockByVariantId(dto.getVariantId())
                .orElseThrow(() -> new EntityNotFoundException("ProductVariant not found with id: " + dto.getVariantId()));

        if (variant.getStock() < dto.getCount()) {
            throw new InsufficientStockException(
                    "Not enough stock for variant " + dto.getVariantId()
                            + ": requested " + dto.getCount() + ", available " + variant.getStock());
        }

        variant.setStock(variant.getStock() - dto.getCount());
        productVariantRepository.save(variant);

        Product product = variant.getProduct();
        long unitPrice = variant.getPriceOverride() != null ? variant.getPriceOverride() : product.getPrice();
        long amount = unitPrice * dto.getCount();

        Purchase purchase = Purchase.builder()
                .buyerId(buyerId)
                .variant(variant)
                .productTitle(product.getTitle())
                .variantLabel(buildVariantLabel(variant))
                .unitPrice(unitPrice)
                .count(dto.getCount())
                .amount(amount)
                .status(PurchaseStatus.PENDING)
                .build();
        purchase = purchaseRepository.saveAndFlush(purchase);

        try {
            UUID transactionId = transactionGrpcClient.chargePurchase(
                    buyerId, amount, purchase.getPurchaseId(), "Покупка: " + product.getTitle());
            purchase.setStatus(PurchaseStatus.CONFIRMED);
            purchase.setTransactionId(transactionId);
        } catch (RuntimeException e) {
            variant.setStock(variant.getStock() + dto.getCount());
            productVariantRepository.save(variant);
            purchase.setStatus(PurchaseStatus.CANCELLED);
            purchaseRepository.saveAndFlush(purchase);
            throw e;
        }

        purchase = purchaseRepository.saveAndFlush(purchase);
        return mapToResponseDTO(purchase);
    }

    /**
     * Вызывается после того, как Transactions Service подтвердит списание баллов.
     */
    @Transactional
    public PurchaseResponseDTO confirmPurchase(UUID purchaseId, UUID transactionId) {
        Purchase purchase = getPendingPurchase(purchaseId);
        purchase.setStatus(PurchaseStatus.CONFIRMED);
        purchase.setTransactionId(transactionId);
        purchase = purchaseRepository.saveAndFlush(purchase);
        return mapToResponseDTO(purchase);
    }

    /**
     * Вызывается, если списание баллов не удалось (например, баланс изменился между
     * резервированием стока и подтверждением) — возвращает сток обратно.
     */
    @Transactional
    public PurchaseResponseDTO cancelPurchase(UUID purchaseId) {
        Purchase purchase = getPendingPurchase(purchaseId);
        restoreStock(purchase);
        purchase.setStatus(PurchaseStatus.CANCELLED);
        purchase = purchaseRepository.saveAndFlush(purchase);
        return mapToResponseDTO(purchase);
    }

    @Transactional
    public PurchaseResponseDTO refundPurchase(UUID purchaseId) {
        Purchase purchase = purchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new EntityNotFoundException("Purchase not found with id: " + purchaseId));
        if (purchase.getStatus() != PurchaseStatus.CONFIRMED) {
            throw new IllegalStateException("Only a confirmed purchase can be refunded: " + purchaseId);
        }
        transactionGrpcClient.refundPurchase(
                purchase.getBuyerId(), purchase.getAmount(), purchase.getPurchaseId(),
                "Возврат: " + purchase.getProductTitle());
        restoreStock(purchase);
        purchase.setStatus(PurchaseStatus.REFUNDED);
        purchase = purchaseRepository.saveAndFlush(purchase);
        return mapToResponseDTO(purchase);
    }

    @Transactional(readOnly = true)
    public PurchaseResponseDTO getPurchaseById(UUID purchaseId) {
        Purchase purchase = purchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new EntityNotFoundException("Purchase not found with id: " + purchaseId));
        return mapToResponseDTO(purchase);
    }

    @Transactional(readOnly = true)
    public List<PurchaseResponseDTO> getPurchasesByBuyerId(long buyerId) {
        return purchaseRepository.findByBuyerId(buyerId).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    private Purchase getPendingPurchase(UUID purchaseId) {
        Purchase purchase = purchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new EntityNotFoundException("Purchase not found with id: " + purchaseId));
        if (purchase.getStatus() != PurchaseStatus.PENDING) {
            throw new IllegalStateException("Purchase " + purchaseId + " is not pending");
        }
        return purchase;
    }

    private void restoreStock(Purchase purchase) {
        ProductVariant variant = productVariantRepository.findWithLockByVariantId(purchase.getVariant().getVariantId())
                .orElseThrow(() -> new EntityNotFoundException("ProductVariant not found with id: " + purchase.getVariant().getVariantId()));
        variant.setStock(variant.getStock() + purchase.getCount());
        productVariantRepository.save(variant);
    }

    private String buildVariantLabel(ProductVariant variant) {
        if (variant.getSize() == null && variant.getColor() == null) {
            return null;
        }
        StringBuilder label = new StringBuilder();
        if (variant.getSize() != null) label.append(variant.getSize());
        if (variant.getColor() != null) {
            if (!label.isEmpty()) label.append(" / ");
            label.append(variant.getColor());
        }
        return label.toString();
    }

    private PurchaseResponseDTO mapToResponseDTO(Purchase purchase) {
        return PurchaseResponseDTO.builder()
                .purchaseId(purchase.getPurchaseId())
                .buyerId(purchase.getBuyerId())
                .variantId(purchase.getVariant().getVariantId())
                .productTitle(purchase.getProductTitle())
                .variantLabel(purchase.getVariantLabel())
                .unitPrice(purchase.getUnitPrice())
                .count(purchase.getCount())
                .amount(purchase.getAmount())
                .status(purchase.getStatus())
                .transactionId(purchase.getTransactionId())
                .description(purchase.getDescription())
                .createdAt(purchase.getCreatedAt())
                .build();
    }
}
