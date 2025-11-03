package com.example.issueservice.controller;

import com.example.issueservice.dto.CreateGroupRequestDTO;
import com.example.issueservice.dto.GroupDTO;
import com.example.issueservice.service.GroupService;
import com.its.commonservice.dto.StandardResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for support group management endpoints
 */
@RestController
@RequestMapping("/api/orgs/{orgId}/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @PostMapping
    public StandardResponse<GroupDTO> createGroup(
            @PathVariable Long orgId,
            @Valid @RequestBody CreateGroupRequestDTO request) {
        
        GroupDTO group = groupService.createGroup(orgId, request);
        return StandardResponse.single(group, "Group created successfully");
    }

    @GetMapping
    public StandardResponse<GroupDTO> getGroups(@PathVariable Long orgId) {
        List<GroupDTO> groups = groupService.getOrganizationGroups(orgId);
        return StandardResponse.list(groups);
    }

    @PostMapping("/{groupId}/members")
    public StandardResponse<Void> addMember(
            @PathVariable Long groupId,
            @RequestParam Long userId) {
        
        groupService.addMemberToGroup(groupId, userId);
        return StandardResponse.single(null, "Member added to group successfully");
    }

    @DeleteMapping("/{groupId}/members/{userId}")
    public StandardResponse<Void> removeMember(
            @PathVariable Long groupId,
            @PathVariable Long userId) {
        
        groupService.removeMemberFromGroup(groupId, userId);
        return StandardResponse.single(null, "Member removed from group successfully");
    }
}
