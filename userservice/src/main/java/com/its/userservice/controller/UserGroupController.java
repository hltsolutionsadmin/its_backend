package com.its.userservice.controller;

import com.its.common.dto.UserGroupDTO;
import com.its.commonservice.dto.StandardResponse;
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
    public StandardResponse<UserGroupDTO> update(@PathVariable Long id, @Valid @RequestBody UserGroupDTO dto) {
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

    @GetMapping("/{projectId}/status/{status}")
    public StandardResponse<UserGroupDTO> getGroupsByProjectAndStatus(
            @PathVariable Long projectId,
            @PathVariable TicketStatus status) {
        // TODO: Implement filtering by TicketStatus in service/repository/model layers.
        // For now, get the first group for the project and return it as single object.
        Page<UserGroupDTO> groups = userGroupService.getGroupsByProjectId(projectId, PageRequest.of(0, 1));
        if (groups.hasContent()) {
            return StandardResponse.single(groups.getContent().get(0), "User group fetched successfully");
        }
        return StandardResponse.error("User group not found for project");
    }
    
    @GetMapping("/{id}")
    public StandardResponse<UserGroupDTO> getById(@PathVariable Long id) {
        UserGroupDTO group = userGroupService.getById(id);
        return StandardResponse.single(group, "User group fetched successfully");
    }
}
