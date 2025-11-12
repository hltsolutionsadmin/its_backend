package com.its.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDTO {
    private Long id;
    private Long organizationId;
    private String name;
    private String description;
    private Boolean active;
    private List<SubCategoryDTO> subCategories;
}
