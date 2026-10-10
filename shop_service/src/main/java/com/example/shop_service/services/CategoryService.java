package com.example.shop_service.services;

import com.example.shop_service.DTOs.CategoryCreateRequestDTO;
import com.example.shop_service.DTOs.CategoryResponseDTO;
import com.example.shop_service.entities.Category;
import com.example.shop_service.exceptions.DuplicateRecordException;
import com.example.shop_service.exceptions.EntityNotFoundException;
import com.example.shop_service.repositories.CategoryRepository;
import com.example.shop_service.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Transactional
    public CategoryResponseDTO createCategory(CategoryCreateRequestDTO dto) {
        Category parent = null;
        if (dto.getParentId() != null) {
            parent = categoryRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + dto.getParentId()));
        }

        boolean duplicate = dto.getParentId() != null
                ? categoryRepository.existsByParent_CategoryIdAndTitleIgnoreCase(dto.getParentId(), dto.getTitle())
                : categoryRepository.existsByParentIsNullAndTitleIgnoreCase(dto.getTitle());
        if (duplicate) {
            throw new DuplicateRecordException("Category with this title already exists under the same parent");
        }

        Category category = Category.builder()
                .parent(parent)
                .title(dto.getTitle())
                .build();

        category = categoryRepository.saveAndFlush(category);
        return mapToResponseDTO(category);
    }

    @Transactional(readOnly = true)
    public CategoryResponseDTO getCategoryById(UUID categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + categoryId));
        return mapToResponseDTO(category);
    }

    @Transactional(readOnly = true)
    public List<CategoryResponseDTO> getCategories(UUID parentId) {
        List<Category> categories = (parentId != null)
                ? categoryRepository.findByParent_CategoryId(parentId)
                : categoryRepository.findByParentIsNull();
        return categories.stream().map(this::mapToResponseDTO).collect(Collectors.toList());
    }

    @Transactional
    public void deleteCategory(UUID categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new EntityNotFoundException("Category not found with id: " + categoryId);
        }
        if (categoryRepository.existsByParent_CategoryId(categoryId)) {
            throw new IllegalStateException("Cannot delete category that has subcategories: " + categoryId);
        }
        if (productRepository.existsByCategory_CategoryIdAndDeletedAtIsNull(categoryId)) {
            throw new IllegalStateException("Cannot delete category that still has products: " + categoryId);
        }
        categoryRepository.deleteById(categoryId);
    }

    private CategoryResponseDTO mapToResponseDTO(Category category) {
        return CategoryResponseDTO.builder()
                .categoryId(category.getCategoryId())
                .parentId(category.getParent() != null ? category.getParent().getCategoryId() : null)
                .title(category.getTitle())
                .createdAt(category.getCreatedAt())
                .build();
    }
}
