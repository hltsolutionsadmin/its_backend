package com.its.userservice.service;

import com.its.common.dto.UserDTO;
import com.its.common.dto.UserGroupDTO;
import com.its.commonservice.enums.TicketPriority;
import com.its.userservice.model.UserGroupModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserGroupService {

    UserGroupDTO create(UserGroupDTO dto);

    UserGroupDTO update(Long id, UserGroupDTO dto);

    void delete(Long id);

    Page<UserGroupDTO> getAll(Pageable pageable);

    UserGroupDTO getById(Long id);

    Page<UserGroupDTO> getGroupsByProjectId(Long projectId, Pageable pageable);

    UserGroupDTO getGroupsByProjectAndPriority(Long projectId, TicketPriority priority);

    void addUserToGroup(Long groupId, Long userId);

    void removeUserFromGroup(Long groupId, Long userId);

    Page<UserDTO> getGroupMembers(Long groupId, int page, int size);
}
