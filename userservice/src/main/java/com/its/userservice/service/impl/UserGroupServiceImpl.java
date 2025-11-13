package com.its.userservice.service.impl;

import com.its.common.dto.UserDTO;
import com.its.common.dto.UserGroupDTO;
import com.its.commonservice.enums.TicketPriority;
import com.its.commonservice.exception.ErrorCode;
import com.its.commonservice.exception.HltCustomerException;
import com.its.userservice.model.UserGroupModel;
import com.its.userservice.model.UserModel;
import com.its.userservice.populator.UserGroupPopulator;
import com.its.userservice.populator.UserPopulator;
import com.its.userservice.repository.UserGroupRepository;
import com.its.userservice.repository.UserRepository;
import com.its.userservice.service.UserGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


@Service
@RequiredArgsConstructor
public class UserGroupServiceImpl implements UserGroupService {

    private final UserGroupRepository userGroupRepository;
    private final UserGroupPopulator userGroupPopulator;
    private final UserRepository userRepository;
    private final UserPopulator userPopulator;


    @Override
    @Transactional
    public UserGroupDTO create(UserGroupDTO dto) {

        validateDuplicateGroup(dto.getGroupName(), dto.getProject().getId());
        boolean exists = userGroupRepository.existsByPriorityAndProjectId(dto.getPriority(),dto.getProject().getId());
        if (exists) {
            throw new HltCustomerException(ErrorCode.GROUP_ALREADY_EXISTS_FOR_PRIORITY);
        }
        UserModel groupLead = resolveGroupLead(dto);

        UserGroupModel model = UserGroupModel.builder()
                .groupName(dto.getGroupName())
                .description(dto.getDescription())
                .projectId(dto.getProject().getId())
                .priority(dto.getPriority())
                .groupLead(groupLead)
                .build();

        userGroupRepository.save(model);
        return userGroupPopulator.toDTO(model);
    }


    @Override
    @Transactional
    public UserGroupDTO update(Long id, UserGroupDTO dto) {
        UserGroupModel model = findGroupById(id);

        if (dto.getGroupName() != null && !dto.getGroupName().isBlank()) {
            model.setGroupName(dto.getGroupName());
        }

        if (dto.getDescription() != null) {
            model.setDescription(dto.getDescription());
        }

        if (dto.getPriority() != null) {
            model.setPriority(dto.getPriority());
        }

        if (dto.getProject() != null && dto.getProject().getId() != null) {
            model.setProjectId(dto.getProject().getId());
        }

        if (dto.getGroupLead() != null && dto.getGroupLead().getId() != null) {
            UserModel groupLead = resolveGroupLead(dto);
            model.setGroupLead(groupLead);
        }

        userGroupRepository.save(model);
        return userGroupPopulator.toDTO(model);
    }

    @Override
    public void delete(Long id) {

    }


    @Override
    @Transactional(readOnly = true)
    public Page<UserGroupDTO> getAll(Pageable pageable) {
        return userGroupRepository.findAll(pageable)
                .map(userGroupPopulator::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public UserGroupDTO getById(Long id) {
        return userGroupPopulator.toDTO(findGroupById(id));
    }

    @Transactional(readOnly = true)
    @Override
    public Page<UserGroupDTO> getGroupsByProjectId(Long projectId, Pageable pageable) {
        return userGroupRepository.findByProjectId(projectId, pageable)
                .map(userGroupPopulator::toDTO);
    }

    @Override
    public UserGroupDTO getGroupsByProjectAndPriority(Long projectId, TicketPriority priority) {
        UserGroupDTO userGroup=userGroupPopulator.toDTO(userGroupRepository.getGroupsByProjectIdAndPriority(projectId, priority).orElseThrow(() -> new HltCustomerException(ErrorCode.GROUP_NOT_FOUND)));
        return userGroup;
    }

    @Override
    public  void addUserToGroup(Long groupId, Long userId) {
        UserGroupModel group = userGroupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));
        UserModel user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        group.getMembers().add(user);
        userGroupRepository.save(group);
    }

    @Override
    public void removeUserFromGroup(Long groupId, Long userId) {
        UserGroupModel group = userGroupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));
        UserModel user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        group.getMembers().remove(user);
         userGroupRepository.save(group);
    }

    @Override
    public Page<UserDTO> getGroupMembers(Long groupId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<UserModel> pageResult = userGroupRepository.findGroupMembers(groupId, pageable);
        return pageResult.map(userPopulator::populate);
    }

    private void validateDuplicateGroup(String groupName, Long projectId) {
        if (userGroupRepository.existsByProjectIdAndGroupNameIgnoreCase(projectId, groupName)) {
            throw new HltCustomerException(ErrorCode.DUPLICATE_GROUP_NAME);
        }
    }


    private UserGroupModel findGroupById(Long id) {
        return userGroupRepository.findById(id)
                .orElseThrow(() -> new HltCustomerException(ErrorCode.GROUP_NOT_FOUND));
    }

    private UserModel resolveGroupLead(UserGroupDTO dto) {
        if (dto.getGroupLead() == null || dto.getGroupLead().getId() == null) {
            return null;
        }

        Optional<UserModel> groupLead = userRepository.findById(dto.getGroupLead().getId());
        if (!groupLead.isPresent()) {
            throw new HltCustomerException(ErrorCode.USER_NOT_FOUND);
        }
        return groupLead.get();
    }
}
