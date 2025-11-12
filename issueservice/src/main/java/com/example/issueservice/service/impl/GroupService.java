package com.example.issueservice.service.impl;

import com.its.common.dto.CreateGroupRequestDTO;
import com.its.common.dto.GroupDTO;
import com.example.issueservice.model.GroupMemberModel;
import com.example.issueservice.model.GroupModel;
import com.example.issueservice.repository.GroupMemberRepository;
import com.example.issueservice.repository.GroupRepository;
import com.its.commonservice.exception.ErrorCode;
import com.its.commonservice.exception.HltCustomerException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for support group management
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;

    @Transactional
    public GroupDTO createGroup(Long orgId, CreateGroupRequestDTO request) {
        log.info("Creating group: {} in organization: {}", request.getName(), orgId);
        
        if (groupRepository.existsByOrganizationIdAndName(orgId, request.getName())) {
            throw new HltCustomerException(ErrorCode.GROUP_NAME_EXISTS);
        }
        
        GroupModel group = new GroupModel();
        group.setOrganizationId(orgId);
        group.setName(request.getName());
        group.setDescription(request.getDescription());
        group.setLevel(request.getLevel());
        group.setActive(true);
        
        group = groupRepository.save(group);
        
        log.info("Group created successfully with ID: {}", group.getId());
        
        return buildGroupDTO(group);
    }

    @Transactional(readOnly = true)
    public List<GroupDTO> getOrganizationGroups(Long orgId) {
        return groupRepository.findByOrganizationId(orgId)
            .stream()
            .map(this::buildGroupDTO)
            .collect(Collectors.toList());
    }

    @Transactional
    public void addMemberToGroup(Long groupId, Long userId) {
        log.info("Adding user {} to group {}", userId, groupId);
        
        GroupModel group = groupRepository.findById(groupId)
            .orElseThrow(() -> new HltCustomerException(ErrorCode.GROUP_NOT_FOUND));
        
        if (groupMemberRepository.existsByGroupIdAndUserId(groupId, userId)) {
            throw new HltCustomerException(ErrorCode.USER_ALREADY_IN_GROUP);
        }
        
        GroupMemberModel member = new GroupMemberModel();
        member.setGroup(group);
        member.setUserId(userId);
        member.setActive(true);
        
        groupMemberRepository.save(member);
        
        log.info("User {} added to group {} successfully", userId, groupId);
    }

    @Transactional
    public void removeMemberFromGroup(Long groupId, Long userId) {
        log.info("Removing user {} from group {}", userId, groupId);
        
        GroupMemberModel member = groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
            .orElseThrow(() -> new HltCustomerException(ErrorCode.USER_NOT_FOUND, 
                "User is not a member of this group"));
        
        groupMemberRepository.delete(member);
        
        log.info("User {} removed from group {} successfully", userId, groupId);
    }

    private GroupDTO buildGroupDTO(GroupModel group) {
        return GroupDTO.builder()
            .id(group.getId())
            .organizationId(group.getOrganizationId())
            .name(group.getName())
            .description(group.getDescription())
            .level(group.getLevel())
            .active(group.getActive())
            .createdAt(group.getCreatedAt())
            .memberCount(group.getMembers() != null ? group.getMembers().size() : 0)
            .build();
    }
}
