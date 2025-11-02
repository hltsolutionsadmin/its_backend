package com.example.issueservice.repository;

import com.example.issueservice.model.GroupMemberModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMemberModel, Long> {
    
    List<GroupMemberModel> findByGroupId(Long groupId);
    
    List<GroupMemberModel> findByUserId(Long userId);
    
    Optional<GroupMemberModel> findByGroupIdAndUserId(Long groupId, Long userId);
    
    boolean existsByGroupIdAndUserId(Long groupId, Long userId);
}
