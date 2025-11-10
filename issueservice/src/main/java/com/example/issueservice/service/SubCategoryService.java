package com.example.issueservice.service;

import com.its.common.dto.SubCategoryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SubCategoryService {
    SubCategoryDTO createSubCategory(SubCategoryDTO dto);
    SubCategoryDTO updateSubCategory(Long id, SubCategoryDTO dto);
    SubCategoryDTO getSubCategory(Long id);
    Page<SubCategoryDTO> getAllSubCategories(Long categoryId, Pageable pageable);
    void deleteSubCategory(Long id);
}
