package com.example.issueservice.repository;

import com.example.issueservice.model.ProjectTechModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectTechRepository extends JpaRepository<ProjectTechModel, Long> {


    Page<ProjectTechModel> findByProjectId(Long projectId, Pageable pageable);

    boolean existsByProjectIdAndTechnologyNameIgnoreCase(Long projectId, String technologyName);

}
