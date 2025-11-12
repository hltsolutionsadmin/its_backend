package com.example.issueservice.service;

import com.its.common.dto.CategoryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CategoryService {
    CategoryDTO createCategory(CategoryDTO dto);
    CategoryDTO updateCategory(Long id, CategoryDTO dto);
    CategoryDTO getCategory(Long id);
    Page<CategoryDTO> getAllCategories(Long orgId, Pageable pageable);
    void deleteCategory(Long id);
}
