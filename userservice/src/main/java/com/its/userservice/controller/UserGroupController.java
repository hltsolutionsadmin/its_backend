package com.its.userservice.controller;

import com.its.common.dto.UserGroupDTO;
import com.its.commonservice.dto.StandardResponse;
import com.its.commonservice.enums.TicketPriority;
import com.its.commonservice.enums.TicketStatus;
import com.its.userservice.service.UserGroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usergroups")
@RequiredArgsConstructor
public class UserGroupController {

    private final UserGroupService userGroupService;

    @PostMapping
    public StandardResponse<UserGroupDTO> create(@Valid @RequestBody UserGroupDTO dto) {
        UserGroupDTO createdGroup = userGroupService.create(dto);
        return StandardResponse.single(createdGroup, "User group created successfully");
    }

    @PutMapping("/{id}")
    public StandardResponse<UserGroupDTO> update(@PathVariable("id") Long id, @Valid @RequestBody UserGroupDTO dto) {
        UserGroupDTO updatedGroup = userGroupService.update(id, dto);
        return StandardResponse.single(updatedGroup, "User group updated successfully");
    }


    @GetMapping
    public StandardResponse<UserGroupDTO> getAll(Pageable pageable) {
        Page<UserGroupDTO> groups = userGroupService.getAll(pageable);
        return StandardResponse.page(groups);
    }

    @GetMapping("/project/{projectId}")
    public StandardResponse<UserGroupDTO> getGroupsByProjectId(
            @PathVariable Long projectId,
            Pageable pageable) {

        Page<UserGroupDTO> groups = userGroupService.getGroupsByProjectId(projectId, pageable);
        return StandardResponse.page(groups);
    }

    @GetMapping("/{projectId}/{priority}")
    public StandardResponse<UserGroupDTO> getGroupsByProjectAndPriority(
            @PathVariable("projectId") Long projectId,
            @PathVariable("priority") TicketPriority priority) {
        UserGroupDTO groups = userGroupService.getGroupsByProjectAndPriority(projectId,priority);
        return StandardResponse.single(groups,"User group not found for project");
    }
    
    @GetMapping("/{id}")
    public StandardResponse<UserGroupDTO> getById(@PathVariable("id") Long id) {
        UserGroupDTO group = userGroupService.getById(id);
        return StandardResponse.single(group, "User group fetched successfully");
    }
}
