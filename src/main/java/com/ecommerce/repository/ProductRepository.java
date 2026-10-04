package com.ecommerce.repository;

import com.ecommerce.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    List<Product> findTop8ByOrderByIdDesc();
    List<Product> findByStockLessThanEqualOrderByStockAsc(int stock);
    List<Product> findTop4ByCategoryIdAndIdNotOrderByIdDesc(Long categoryId, Long excludeId);
}
