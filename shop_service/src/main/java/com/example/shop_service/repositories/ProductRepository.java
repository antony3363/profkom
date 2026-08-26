package com.example.shop_service.repositories;

import com.example.shop_service.entities.Product;
import com.example.shop_service.enums.ProductStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
    List<Product> findByStatusAndDeletedAtIsNull(ProductStatus status);
    List<Product> findByCategory_CategoryIdAndStatusAndDeletedAtIsNull(UUID categoryId, ProductStatus status);
    boolean existsBySlug(String slug);
}
