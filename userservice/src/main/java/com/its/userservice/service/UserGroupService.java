package com.its.userservice.service;

import com.its.common.dto.UserGroupDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserGroupService {

    UserGroupDTO create(UserGroupDTO dto);

    UserGroupDTO update(Long id, UserGroupDTO dto);

    void delete(Long id);

    Page<UserGroupDTO> getAll(Pageable pageable);

    UserGroupDTO getById(Long id);

    Page<UserGroupDTO> getGroupsByProjectId(Long projectId, Pageable pageable);
}
