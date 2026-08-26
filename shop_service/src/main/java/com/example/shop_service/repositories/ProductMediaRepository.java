package com.example.shop_service.repositories;

import com.example.shop_service.entities.ProductMedia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductMediaRepository extends JpaRepository<ProductMedia, UUID> {
    List<ProductMedia> findByProduct_ProductIdOrderBySortOrderAsc(UUID productId);
}
