package com.example.shop_service.services;

import com.example.shop_service.DTOs.*;
import com.example.shop_service.entities.Category;
import com.example.shop_service.entities.Product;
import com.example.shop_service.entities.ProductVariant;
import com.example.shop_service.enums.ProductStatus;
import com.example.shop_service.exceptions.EntityNotFoundException;
import com.example.shop_service.repositories.CategoryRepository;
import com.example.shop_service.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public ProductResponseDTO createProduct(ProductCreateRequestDTO dto) {
        Category category = null;
        if (dto.getCategoryId() != null) {
            category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + dto.getCategoryId()));
        }

        Product product = Product.builder()
                .category(category)
                .title(dto.getTitle())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .status(ProductStatus.DRAFT)
                .build();

        Product finalProduct = product;
        List<ProductVariant> variants = dto.getVariants().stream()
                .map(v -> ProductVariant.builder()
                        .product(finalProduct)
                        .size(v.getSize())
                        .color(v.getColor())
                        .stock(v.getStock())
                        .priceOverride(v.getPriceOverride())
                        .build())
                .collect(Collectors.toList());
        product.setVariants(variants);

        product = productRepository.saveAndFlush(product);
        return mapToResponseDTO(product);
    }

    @Transactional(readOnly = true)
    public ProductResponseDTO getProductById(UUID productId) {
        Product product = findActiveProduct(productId);
        return mapToResponseDTO(product);
    }

    @Transactional(readOnly = true)
    public List<ProductCatalogDTO> getAllProductsForCatalog(UUID categoryId) {
        List<Product> products = (categoryId != null)
                ? productRepository.findByCategory_CategoryIdAndStatusAndDeletedAtIsNull(categoryId, ProductStatus.PUBLISHED)
                : productRepository.findByStatusAndDeletedAtIsNull(ProductStatus.PUBLISHED);
        return products.stream().map(this::mapToCatalogDTO).collect(Collectors.toList());
    }

    @Transactional
    public ProductResponseDTO updateProduct(UUID productId, ProductUpdateRequestDTO dto) {
        Product product = findActiveProduct(productId);

        if (dto.getTitle() != null) product.setTitle(dto.getTitle());
        if (dto.getDescription() != null) product.setDescription(dto.getDescription());
        if (dto.getPrice() != null) product.setPrice(dto.getPrice());
        if (dto.getCoverMediaId() != null) product.setCoverMediaId(dto.getCoverMediaId());
        if (dto.getStatus() != null) product.setStatus(dto.getStatus());
        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + dto.getCategoryId()));
            product.setCategory(category);
        }

        product = productRepository.saveAndFlush(product);
        return mapToResponseDTO(product);
    }

    @Transactional
    public void deleteProduct(UUID productId) {
        Product product = findActiveProduct(productId);
        product.setDeletedAt(LocalDateTime.now());
        product.setStatus(ProductStatus.ARCHIVED);
        productRepository.saveAndFlush(product);
    }

    private Product findActiveProduct(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + productId));
        if (product.getDeletedAt() != null) {
            throw new EntityNotFoundException("Product not found with id: " + productId);
        }
        return product;
    }

    private ProductResponseDTO mapToResponseDTO(Product product) {
        List<ProductVariantResponseDTO> variants = product.getVariants().stream()
                .map(this::mapVariantToResponseDTO)
                .collect(Collectors.toList());

        return ProductResponseDTO.builder()
                .productId(product.getProductId())
                .categoryId(product.getCategory() != null ? product.getCategory().getCategoryId() : null)
                .title(product.getTitle())
                .description(product.getDescription())
                .price(product.getPrice())
                .status(product.getStatus())
                .coverMediaId(product.getCoverMediaId())
                .variants(variants)
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    private ProductVariantResponseDTO mapVariantToResponseDTO(ProductVariant variant) {
        return ProductVariantResponseDTO.builder()
                .variantId(variant.getVariantId())
                .productId(variant.getProduct().getProductId())
                .size(variant.getSize())
                .color(variant.getColor())
                .stock(variant.getStock())
                .priceOverride(variant.getPriceOverride())
                .createdAt(variant.getCreatedAt())
                .build();
    }

    private ProductCatalogDTO mapToCatalogDTO(Product product) {
        int totalStock = product.getVariants().stream().mapToInt(ProductVariant::getStock).sum();
        return ProductCatalogDTO.builder()
                .productId(product.getProductId())
                .title(product.getTitle())
                .price(product.getPrice())
                .coverMediaId(product.getCoverMediaId())
                .totalStock(totalStock)
                .build();
    }
}
