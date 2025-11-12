package com.example.issueservice.controller;


import com.its.common.dto.CategoryDTO;
import com.example.issueservice.service.CategoryService;
import com.its.commonservice.dto.StandardResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public StandardResponse<CategoryDTO> create(@RequestBody CategoryDTO dto) {
        CategoryDTO created = categoryService.createCategory(dto);
        return StandardResponse.single(created, "Category created successfully");
    }

    @PutMapping("/{id}")
    public StandardResponse<CategoryDTO> update(@PathVariable("id") Long id, @RequestBody CategoryDTO dto) {
        CategoryDTO updated = categoryService.updateCategory(id, dto);
        return StandardResponse.single(updated, "Category updated successfully");
    }

    @GetMapping("/{id}")
    public StandardResponse<CategoryDTO> get(@PathVariable("id") Long id) {
        CategoryDTO category = categoryService.getCategory(id);
        return StandardResponse.single(category, "Category fetched successfully");
    }

    @GetMapping("/org/{orgId}")
    public StandardResponse<CategoryDTO> list(
            @PathVariable("orgId") Long orgId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<CategoryDTO> categories = categoryService.getAllCategories(orgId, pageable);
        return StandardResponse.page(categories);
    }


    @DeleteMapping("/{id}")
    public StandardResponse<String> delete(@PathVariable("id") Long id) {
        categoryService.deleteCategory(id);
        return StandardResponse.message("Category deleted successfully");
    }
}
