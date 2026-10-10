package com.example.shop_service.controllers;

import com.example.shop_service.DTOs.CategoryCreateRequestDTO;
import com.example.shop_service.DTOs.CategoryResponseDTO;
import com.example.shop_service.exceptions.UnauthorizedException;
import com.example.shop_service.services.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private static final String ROLE_ADMIN = "ADMIN";

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<CategoryResponseDTO> createCategory(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @Valid @RequestBody CategoryCreateRequestDTO dto) {
        requireAdmin(role);
        CategoryResponseDTO response = categoryService.createCategory(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryResponseDTO> getCategory(@PathVariable UUID categoryId) {
        return ResponseEntity.ok(categoryService.getCategoryById(categoryId));
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponseDTO>> getCategories(
            @RequestParam(required = false) UUID parentId) {
        return ResponseEntity.ok(categoryService.getCategories(parentId));
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Void> deleteCategory(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @PathVariable UUID categoryId) {
        requireAdmin(role);
        categoryService.deleteCategory(categoryId);
        return ResponseEntity.noContent().build();
    }

    private void requireAdmin(String role) {
        if (!ROLE_ADMIN.equals(role)) {
            throw new UnauthorizedException("Только администратор управляет справочником категорий");
        }
    }
}
