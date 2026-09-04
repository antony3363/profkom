package com.example.shop_service.services;

import com.example.shop_service.DTOs.ProductMediaCreateRequestDTO;
import com.example.shop_service.DTOs.ProductMediaResponseDTO;
import com.example.shop_service.entities.Product;
import com.example.shop_service.entities.ProductMedia;
import com.example.shop_service.exceptions.EntityNotFoundException;
import com.example.shop_service.exceptions.UnsupportedMediaException;
import com.example.shop_service.repositories.ProductMediaRepository;
import com.example.shop_service.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductMediaService {

    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");

    private final ProductMediaRepository productMediaRepository;
    private final ProductRepository productRepository;

    @Transactional
    public ProductMediaResponseDTO addMedia(ProductMediaCreateRequestDTO dto) {
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + dto.getProductId()));

        requirePhoto(dto.getMedia());

        ProductMedia media = ProductMedia.builder()
                .product(product)
                .media(dto.getMedia())
                .isCover(dto.isCover())
                .sortOrder(dto.getSortOrder())
                .build();

        media = productMediaRepository.saveAndFlush(media);

        if (media.isCover()) {
            product.setCoverMediaId(media.getMediaId());
            productRepository.saveAndFlush(product);
        }

        return mapToResponseDTO(media);
    }

    @Transactional(readOnly = true)
    public List<ProductMediaResponseDTO> getMediaByProductId(UUID productId) {
        return productMediaRepository.findByProduct_ProductIdOrderBySortOrderAsc(productId).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteMedia(UUID mediaId) {
        if (!productMediaRepository.existsById(mediaId)) {
            throw new EntityNotFoundException("ProductMedia not found with id: " + mediaId);
        }
        productMediaRepository.deleteById(mediaId);
    }

    private void requirePhoto(String url) {
        String lower = url.toLowerCase();
        int dot = lower.lastIndexOf('.');
        String extension = dot >= 0 ? lower.substring(dot + 1) : "";
        if (!ALLOWED_IMAGE_EXTENSIONS.contains(extension)) {
            throw new UnsupportedMediaException(
                    "Only photo uploads are allowed (jpg, jpeg, png, webp), got: " + url);
        }
    }

    private ProductMediaResponseDTO mapToResponseDTO(ProductMedia media) {
        return ProductMediaResponseDTO.builder()
                .mediaId(media.getMediaId())
                .productId(media.getProduct().getProductId())
                .media(media.getMedia())
                .isCover(media.isCover())
                .sortOrder(media.getSortOrder())
                .createdAt(media.getCreatedAt())
                .build();
    }
}
