package com.example.issueservice.service.impl;


import com.its.common.dto.CategoryDTO;
import com.its.commonservice.exception.ErrorCode;
import com.example.issueservice.model.CategoryModel;
import com.example.issueservice.populator.CategoryPopulator;
import com.example.issueservice.repository.CategoryRepository;
import com.example.issueservice.service.CategoryService;
import com.its.commonservice.exception.HltCustomerException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryPopulator categoryPopulator;

    @Override
    public CategoryDTO createCategory(CategoryDTO dto) {
        log.info("Creating category for organizationId={} with name={}", dto.getOrganizationId(), dto.getName());

        if (categoryRepository.existsByOrganizationIdAndNameIgnoreCase(dto.getOrganizationId(), dto.getName())) {
            throw new HltCustomerException(ErrorCode.DUPLICATE_CATEGORY);
        }

        CategoryModel model = new CategoryModel();
        categoryPopulator.populate(dto, model);
        categoryRepository.save(model);

        CategoryDTO result = new CategoryDTO();
        categoryPopulator.populate(model, result);
        log.info("Category created successfully with id={}", model.getId());
        return result;
    }

    @Override
    public CategoryDTO updateCategory(Long id, CategoryDTO dto) {
        log.info("Updating category id={}", id);

        CategoryModel model = categoryRepository.findById(id)
                .orElseThrow(() -> new HltCustomerException(ErrorCode.CATEGORY_NOT_FOUND));

        categoryPopulator.populate(dto, model);
        categoryRepository.save(model);

        CategoryDTO result = new CategoryDTO();
        categoryPopulator.populate(model, result);
        log.info("Category updated successfully for id={}", id);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDTO getCategory(Long id) {
        log.info("Fetching category by id={}", id);

        CategoryModel model = categoryRepository.findById(id)
                .orElseThrow(() -> new HltCustomerException(ErrorCode.CATEGORY_NOT_FOUND));

        CategoryDTO dto = new CategoryDTO();
        categoryPopulator.populate(model, dto);
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CategoryDTO> getAllCategories(Long orgId, Pageable pageable) {
        log.info("Fetching all categories for organizationId={} page={} size={}",
                orgId, pageable.getPageNumber(), pageable.getPageSize());

        return categoryRepository.findByOrganizationId(orgId, pageable)
                .map(model -> {
                    CategoryDTO dto = new CategoryDTO();
                    categoryPopulator.populate(model, dto);
                    return dto;
                });
    }

    @Override
    public void deleteCategory(Long id) {
        log.info("Deleting category id={}", id);

        CategoryModel model = categoryRepository.findById(id)
                .orElseThrow(() -> new HltCustomerException(ErrorCode.CATEGORY_NOT_FOUND));

        categoryRepository.delete(model);
        log.info("Category deleted successfully id={}", id);
    }
}
