package com.example.shop_service.controllers;

import com.example.shop_service.DTOs.ProductMediaCreateRequestDTO;
import com.example.shop_service.DTOs.ProductMediaResponseDTO;
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

    private final ProductMediaService productMediaService;

    @PostMapping
    public ResponseEntity<ProductMediaResponseDTO> addMedia(@Valid @RequestBody ProductMediaCreateRequestDTO dto) {
        ProductMediaResponseDTO response = productMediaService.addMedia(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ProductMediaResponseDTO>> getMedia(@RequestParam UUID productId) {
        return ResponseEntity.ok(productMediaService.getMediaByProductId(productId));
    }

    @DeleteMapping("/{mediaId}")
    public ResponseEntity<Void> deleteMedia(@PathVariable UUID mediaId) {
        productMediaService.deleteMedia(mediaId);
        return ResponseEntity.noContent().build();
    }
}
