package com.example.issueservice.repository;

import com.example.issueservice.model.GroupModel;
import com.juvarya.commonservice.enums.GroupLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<GroupModel, Long> {
    
    List<GroupModel> findByOrganizationId(Long organizationId);
    
    List<GroupModel> findByOrganizationIdAndLevel(Long organizationId, GroupLevel level);
    
    Optional<GroupModel> findByOrganizationIdAndName(Long organizationId, String name);
    
    boolean existsByOrganizationIdAndName(Long organizationId, String name);
}
