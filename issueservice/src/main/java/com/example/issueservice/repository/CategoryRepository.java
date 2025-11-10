package com.example.issueservice.repository;

import com.example.issueservice.model.CategoryModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<CategoryModel, Long> {
    boolean existsByOrganizationIdAndNameIgnoreCase(Long organizationId, String name);
    Page<CategoryModel> findByOrganizationId(Long organizationId, Pageable pageable);


    @EntityGraph(attributePaths = "subCategories")
    @Query("SELECT c FROM CategoryModel c WHERE c.id = :id")
    Optional<CategoryModel> findByIdWithSubCategories(Long id);

}
