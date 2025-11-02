package com.example.issueservice.repository;

import com.example.issueservice.model.ProjectMemberModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMemberModel, Long> {
    
    List<ProjectMemberModel> findByProjectId(Long projectId);
    
    List<ProjectMemberModel> findByUserId(Long userId);
    
    Optional<ProjectMemberModel> findByProjectIdAndUserId(Long projectId, Long userId);
    
    boolean existsByProjectIdAndUserId(Long projectId, Long userId);
}
