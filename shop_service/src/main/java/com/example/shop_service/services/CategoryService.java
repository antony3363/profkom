package com.example.shop_service.services;

import com.example.shop_service.DTOs.CategoryCreateRequestDTO;
import com.example.shop_service.DTOs.CategoryResponseDTO;
import com.example.shop_service.entities.Category;
import com.example.shop_service.exceptions.DuplicateRecordException;
import com.example.shop_service.exceptions.EntityNotFoundException;
import com.example.shop_service.repositories.CategoryRepository;
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

    @Transactional
    public CategoryResponseDTO createCategory(CategoryCreateRequestDTO dto) {
        if (categoryRepository.existsBySlug(dto.getSlug())) {
            throw new DuplicateRecordException("Category with slug " + dto.getSlug() + " already exists");
        }

        Category parent = null;
        if (dto.getParentId() != null) {
            parent = categoryRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + dto.getParentId()));
        }

        Category category = Category.builder()
                .parent(parent)
                .title(dto.getTitle())
                .slug(dto.getSlug())
                .build();

        category = categoryRepository.save(category);
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
        categoryRepository.deleteById(categoryId);
    }

    private CategoryResponseDTO mapToResponseDTO(Category category) {
        return CategoryResponseDTO.builder()
                .categoryId(category.getCategoryId())
                .parentId(category.getParent() != null ? category.getParent().getCategoryId() : null)
                .title(category.getTitle())
                .slug(category.getSlug())
                .createdAt(category.getCreatedAt())
                .build();
    }
}
