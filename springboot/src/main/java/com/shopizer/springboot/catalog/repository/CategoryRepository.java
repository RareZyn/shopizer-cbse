package com.shopizer.springboot.catalog.repository;

import com.shopizer.springboot.catalog.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Category Repository Interface
 * FR-002: Category CRUD operations
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByIsActiveTrue();
}
