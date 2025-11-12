package com.its.userservice.repository;

import com.its.commonservice.enums.TicketPriority;
import com.its.userservice.model.UserGroupModel;
import com.netflix.appinfo.ApplicationInfoManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserGroupRepository extends JpaRepository<UserGroupModel, Long> {

    Page<UserGroupModel> findByProjectId(Long projectId, Pageable pageable);

    boolean existsByProjectIdAndGroupNameIgnoreCase(Long projectId, String groupName);

    Optional<UserGroupModel> findByPriorityAndProjectId(TicketPriority priority, Long projectId);

    boolean existsByPriorityAndProjectId(TicketPriority priority, Long projectId);

    Optional<UserGroupModel>  getGroupsByProjectIdAndPriority(Long projectId, TicketPriority priority);
}
