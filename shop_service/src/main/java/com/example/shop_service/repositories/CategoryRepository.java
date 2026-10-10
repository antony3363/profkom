package com.example.shop_service.repositories;

import com.example.shop_service.entities.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {
    List<Category> findByParent_CategoryId(UUID parentId);
    List<Category> findByParentIsNull();
    boolean existsByParent_CategoryId(UUID parentId);
    boolean existsByParentIsNullAndTitleIgnoreCase(String title);
    boolean existsByParent_CategoryIdAndTitleIgnoreCase(UUID parentId, String title);
}
