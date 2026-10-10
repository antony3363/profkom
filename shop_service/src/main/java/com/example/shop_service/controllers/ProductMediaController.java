package com.example.shop_service.controllers;

import com.example.shop_service.DTOs.ProductMediaCreateRequestDTO;
import com.example.shop_service.DTOs.ProductMediaResponseDTO;
import com.example.shop_service.exceptions.UnauthorizedException;
import com.example.shop_service.services.ProductMediaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/product-media")
@RequiredArgsConstructor
public class ProductMediaController {

    private static final String ROLE_ADMIN = "ADMIN";

    private final ProductMediaService productMediaService;

    @PostMapping
    public ResponseEntity<ProductMediaResponseDTO> addMedia(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @Valid @RequestBody ProductMediaCreateRequestDTO dto) {
        requireAdmin(role);
        ProductMediaResponseDTO response = productMediaService.addMedia(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ProductMediaResponseDTO>> getMedia(@RequestParam UUID productId) {
        return ResponseEntity.ok(productMediaService.getMediaByProductId(productId));
    }

    @DeleteMapping("/{mediaId}")
    public ResponseEntity<Void> deleteMedia(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @PathVariable UUID mediaId) {
        requireAdmin(role);
        productMediaService.deleteMedia(mediaId);
        return ResponseEntity.noContent().build();
    }

    private void requireAdmin(String role) {
        if (!ROLE_ADMIN.equals(role)) {
            throw new UnauthorizedException("Только администратор управляет медиа товаров");
        }
    }
}
