package com.its.userservice.service;

import com.its.userservice.dto.CreateOrganizationRequestDTO;
import com.its.userservice.dto.InviteUserRequestDTO;
import com.its.userservice.dto.OrganizationDTO;
import com.its.userservice.model.OrganizationModel;
import com.its.userservice.model.OrganizationUserModel;
import com.its.userservice.model.UserModel;
import com.its.userservice.populator.OrganizationPopulator;
import com.its.userservice.repository.OrganizationRepository;
import com.its.userservice.repository.OrganizationUserRepository;
import com.its.userservice.repository.UserRepository;
import com.its.commonservice.enums.UserRole;
import com.its.commonservice.exception.ErrorCode;
import com.its.commonservice.exception.HltCustomerException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for organization management operations
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final OrganizationUserRepository organizationUserRepository;
    private final OrganizationPopulator organizationPopulator;

    @Transactional
    public OrganizationDTO createOrganization(CreateOrganizationRequestDTO request, Long ownerId) {
        log.info("Creating organization with code: {}", request.getOrgCode());
        
        if (organizationRepository.existsByOrgCode(request.getOrgCode())) {
            throw new HltCustomerException(ErrorCode.ORG_CODE_TAKEN);
        }
        
        UserModel owner = userRepository.findById(ownerId)
            .orElseThrow(() -> new HltCustomerException(ErrorCode.USER_NOT_FOUND));
        
        OrganizationModel organization = new OrganizationModel();
        organization.setName(request.getName());
        organization.setOrgCode(request.getOrgCode().toUpperCase());
        organization.setDescription(request.getDescription());
        organization.setWebsite(request.getWebsite());
        organization.setAddress(request.getAddress());
        organization.setCity(request.getCity());
        organization.setCountry(request.getCountry());
        organization.setActive(true);
        organization.setOwner(owner);
        
        organization = organizationRepository.save(organization);
        
        // Add owner as ORG_ADMIN
        OrganizationUserModel orgUser = new OrganizationUserModel();
        orgUser.setOrganization(organization);
        orgUser.setUser(owner);
        orgUser.setRole(UserRole.ORG_ADMIN);
        orgUser.setActive(true);
        orgUser.setInvitedBy(owner);
        organizationUserRepository.save(orgUser);
        
        log.info("Organization created successfully with ID: {}", organization.getId());
        
        return organizationPopulator.populate(organization);
    }

    @Transactional(readOnly = true)
    public OrganizationDTO getOrganizationById(Long orgId, Long userId) {
        log.debug("Fetching organization with ID: {}", orgId);
        
        OrganizationModel organization = organizationRepository.findById(orgId)
            .orElseThrow(() -> new HltCustomerException(ErrorCode.ORG_NOT_FOUND));
        
        // Verify user is a member
        if (!organizationUserRepository.existsByOrganizationIdAndUserId(orgId, userId)) {
            throw new HltCustomerException(ErrorCode.USER_NOT_IN_ORG);
        }
        
        return organizationPopulator.populate(organization);
    }

    @Transactional(readOnly = true)
    public List<OrganizationDTO> getUserOrganizations(Long userId) {
        log.debug("Fetching organizations for user: {}", userId);
        
        List<OrganizationUserModel> orgUsers = organizationUserRepository.findByUserId(userId);
        
        return orgUsers.stream()
            .map(ou -> organizationPopulator.populate(ou.getOrganization()))
            .collect(Collectors.toList());
    }

    @Transactional
    public void inviteUserToOrganization(Long orgId, InviteUserRequestDTO request, Long inviterId) {
        log.info("Inviting user {} to organization {}", request.getEmail(), orgId);
        
        OrganizationModel organization = organizationRepository.findById(orgId)
            .orElseThrow(() -> new HltCustomerException(ErrorCode.ORG_NOT_FOUND));
        
        // Verify inviter is ORG_ADMIN
        OrganizationUserModel inviterOrgUser = organizationUserRepository
            .findByOrganizationIdAndUserId(orgId, inviterId)
            .orElseThrow(() -> new HltCustomerException(ErrorCode.USER_NOT_IN_ORG));
        
        if (inviterOrgUser.getRole() != UserRole.ORG_ADMIN && 
            inviterOrgUser.getRole() != UserRole.SUPER_ADMIN) {
            throw new HltCustomerException(ErrorCode.NOT_ORG_ADMIN);
        }
        
        // Find or create user
        UserModel invitee = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new HltCustomerException(ErrorCode.USER_NOT_FOUND, 
                "User with email " + request.getEmail() + " not found. They must register first."));
        
        // Check if already a member
        if (organizationUserRepository.existsByOrganizationIdAndUserId(orgId, invitee.getId())) {
            throw new HltCustomerException(ErrorCode.USER_ALREADY_IN_ORG);
        }
        
        OrganizationUserModel orgUser = new OrganizationUserModel();
        orgUser.setOrganization(organization);
        orgUser.setUser(invitee);
        orgUser.setRole(request.getRole());
        orgUser.setActive(true);
        orgUser.setInvitedBy(inviterOrgUser.getUser());
        
        organizationUserRepository.save(orgUser);
        
        log.info("User {} invited to organization {} successfully", invitee.getId(), orgId);
    }

    @Transactional
    public void removeUserFromOrganization(Long orgId, Long userId, Long removerId) {
        log.info("Removing user {} from organization {}", userId, orgId);
        
        // Verify remover is ORG_ADMIN
        OrganizationUserModel removerOrgUser = organizationUserRepository
            .findByOrganizationIdAndUserId(orgId, removerId)
            .orElseThrow(() -> new HltCustomerException(ErrorCode.USER_NOT_IN_ORG));
        
        if (removerOrgUser.getRole() != UserRole.ORG_ADMIN && 
            removerOrgUser.getRole() != UserRole.SUPER_ADMIN) {
            throw new HltCustomerException(ErrorCode.NOT_ORG_ADMIN);
        }
        
        OrganizationUserModel targetOrgUser = organizationUserRepository
            .findByOrganizationIdAndUserId(orgId, userId)
            .orElseThrow(() -> new HltCustomerException(ErrorCode.USER_NOT_IN_ORG));
        
        organizationUserRepository.delete(targetOrgUser);
        
        log.info("User {} removed from organization {} successfully", userId, orgId);
    }

    @Transactional
    public OrganizationDTO updateOrganization(Long orgId, CreateOrganizationRequestDTO request, Long userId) {
        log.info("Updating organization with ID: {}", orgId);
        
        OrganizationModel organization = organizationRepository.findById(orgId)
            .orElseThrow(() -> new HltCustomerException(ErrorCode.ORG_NOT_FOUND));
        
        // Verify user is ORG_ADMIN
        OrganizationUserModel orgUser = organizationUserRepository
            .findByOrganizationIdAndUserId(orgId, userId)
            .orElseThrow(() -> new HltCustomerException(ErrorCode.USER_NOT_IN_ORG));
        
        if (orgUser.getRole() != UserRole.ORG_ADMIN && 
            orgUser.getRole() != UserRole.SUPER_ADMIN) {
            throw new HltCustomerException(ErrorCode.NOT_ORG_ADMIN);
        }
        
        if (request.getName() != null) {
            organization.setName(request.getName());
        }
        if (request.getDescription() != null) {
            organization.setDescription(request.getDescription());
        }
        if (request.getWebsite() != null) {
            organization.setWebsite(request.getWebsite());
        }
        if (request.getAddress() != null) {
            organization.setAddress(request.getAddress());
        }
        if (request.getCity() != null) {
            organization.setCity(request.getCity());
        }
        if (request.getCountry() != null) {
            organization.setCountry(request.getCountry());
        }
        
        organization = organizationRepository.save(organization);
        
        log.info("Organization updated successfully: {}", orgId);
        
        return organizationPopulator.populate(organization);
    }
}
