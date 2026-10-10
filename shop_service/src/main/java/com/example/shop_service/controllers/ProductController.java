package com.example.shop_service.controllers;

import com.example.shop_service.DTOs.*;
import com.example.shop_service.exceptions.UnauthorizedException;
import com.example.shop_service.services.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private static final String ROLE_ADMIN = "ADMIN";

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ProductResponseDTO> createProduct(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @Valid @RequestBody ProductCreateRequestDTO dto) {
        requireAdmin(role);
        ProductResponseDTO response = productService.createProduct(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponseDTO> getProduct(@PathVariable UUID productId) {
        return ResponseEntity.ok(productService.getProductById(productId));
    }

    @GetMapping
    public ResponseEntity<List<ProductCatalogDTO>> getCatalog(
            @RequestParam(required = false) UUID categoryId) {
        return ResponseEntity.ok(productService.getAllProductsForCatalog(categoryId));
    }

    @PutMapping("/{productId}")
    public ResponseEntity<ProductResponseDTO> updateProduct(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @PathVariable UUID productId,
            @Valid @RequestBody ProductUpdateRequestDTO dto) {
        requireAdmin(role);
        return ResponseEntity.ok(productService.updateProduct(productId, dto));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @PathVariable UUID productId) {
        requireAdmin(role);
        productService.deleteProduct(productId);
        return ResponseEntity.noContent().build();
    }

    private void requireAdmin(String role) {
        if (!ROLE_ADMIN.equals(role)) {
            throw new UnauthorizedException("Только администратор управляет каталогом товаров");
        }
    }
}
