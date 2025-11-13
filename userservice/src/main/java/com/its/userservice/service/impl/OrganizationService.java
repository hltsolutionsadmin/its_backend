package com.its.userservice.service.impl;

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
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.security.SecureRandom;

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
    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;

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
        orgUser.setUserRole(roleService.getByName(UserRole.ORG_ADMIN));
        orgUser.setActive(true);
        orgUser.setInvitedBy(owner);
        organizationUserRepository.save(orgUser);

        log.info("Organization created successfully with ID: {}", organization.getId());

        return organizationPopulator.populate(organization);
    }

    // Helper: generate a unique username from email local-part, suffixing with numbers if needed
    private String generateUniqueUsernameFromEmail(String email) {
        String local = email.split("@")[0].replaceAll("[^a-zA-Z0-9._-]", "");
        if (local.isBlank()) {
            local = "user";
        }
        String base = local.length() > 30 ? local.substring(0, 30) : local; // respect typical limits
        String candidate = base;
        int suffix = 1;
        while (userRepository.existsByUsername(candidate)) {
            String s = String.valueOf(suffix++);
            int maxBase = Math.max(1, 50 - s.length()); // UserModel.username length = 50
            candidate = base.substring(0, Math.min(base.length(), maxBase)) + s;
        }
        return candidate;
    }

    // Helper: generate a secure temporary password
    private String generateTemporaryPassword() {
        final String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-_=+";
        SecureRandom rnd = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
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
    public void inviteUserToOrganization(Long orgId, InviteUserRequestDTO request, Long inviterId, Long userId) {
        log.info("Inviting user {} to organization {}", request.getEmail(), orgId);

        OrganizationModel organization = organizationRepository.findById(orgId)
            .orElseThrow(() -> new HltCustomerException(ErrorCode.ORG_NOT_FOUND));

        // Verify inviter is ORG_ADMIN
        OrganizationUserModel inviterOrgUser = organizationUserRepository
            .findByOrganizationIdAndUserId(orgId, inviterId)
            .orElseThrow(() -> new HltCustomerException(ErrorCode.USER_NOT_IN_ORG));

        if (inviterOrgUser.getUserRole().getRole() != UserRole.ORG_ADMIN && inviterOrgUser.getUserRole().getRole()!= UserRole.SUPER_ADMIN) {
            throw new HltCustomerException(ErrorCode.NOT_ORG_ADMIN);
        }
        Optional<UserModel> invitee=userRepository.findById(userId);
        UserModel user=null;
        if(!invitee.isPresent()){
            user=findByEmailIfNotExistCreateNewUser(request);
        }
        else {
            user=invitee.get();
        }


        // Check if already a member
        if (organizationUserRepository.existsByOrganizationIdAndUserId(orgId, user.getId())) {
            throw new HltCustomerException(ErrorCode.USER_ALREADY_IN_ORG);
        }

        OrganizationUserModel orgUser = new OrganizationUserModel();
        orgUser.setOrganization(organization);
        orgUser.setUser(user);
        orgUser.setUserRole(roleService.getByName(request.getRole()));
        orgUser.setActive(true);
        orgUser.setInvitedBy(inviterOrgUser.getUser());

        organizationUserRepository.save(orgUser);

        log.info("User {} invited to organization {} successfully", user.getId(), orgId);
    }

    private UserModel findByEmailIfNotExistCreateNewUser(InviteUserRequestDTO request) {
        return userRepository.findByEmail(request.getEmail())
                .orElseGet(() -> {
                    log.info("User with email {} not found. Creating a new user for invite.", request.getEmail());
                    UserModel newUser = new UserModel();
                    newUser.setEmail(request.getEmail());
                    newUser.setUsername(request.getEmail());
                    newUser.setPasswordHash(passwordEncoder.encode(request.getPassword()));
                    newUser.setActive(true);
                    newUser.setEmailVerified(false);
                    newUser.setFirstName(request.getFirstName());
                    newUser.setLastName(request.getLastName());
                    newUser.setPhone(request.getPhone());
                    newUser.setCreatedAt(Instant.now());
                    newUser.setUpdatedAt(Instant.now());
                    // Names/phone can be set later by the user after activation
                    return userRepository.save(newUser);
                });
    }

    @Transactional
    public void removeUserFromOrganization(Long orgId, Long userId, Long removerId) {
        log.info("Removing user {} from organization {}", userId, orgId);

        // Verify remover is ORG_ADMIN
        OrganizationUserModel removerOrgUser = organizationUserRepository
            .findByOrganizationIdAndUserId(orgId, removerId)
            .orElseThrow(() -> new HltCustomerException(ErrorCode.USER_NOT_IN_ORG));

        if (removerOrgUser.getUserRole().getRole() != UserRole.ORG_ADMIN &&
            removerOrgUser.getUserRole().getRole() != UserRole.SUPER_ADMIN) {
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

        if (orgUser.getUserRole().getRole() != UserRole.ORG_ADMIN &&
            orgUser.getUserRole().getRole() != UserRole.SUPER_ADMIN) {
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

    public void updateUserOrgRole(Long orgId, Long userId, String roleName) {
        OrganizationUserModel orgUser = organizationUserRepository
                .findByOrganizationIdAndUserId(orgId, userId)
                .orElseThrow(() -> new HltCustomerException(ErrorCode.USER_NOT_IN_ORG));
        if (roleName != null) {
            orgUser.setUserRole(roleService.getByName(UserRole.valueOf(roleName)));
            organizationUserRepository.save(orgUser);
        }
    }


    public void removeUserOrgRole(Long orgId, Long userId, String roleName) {
        OrganizationUserModel orgUser = organizationUserRepository
                .findByOrganizationIdAndUserId(orgId, userId)
                .orElseThrow(() -> new HltCustomerException(ErrorCode.USER_NOT_IN_ORG));

        // Compare enum names safely
        if (orgUser!=null&&orgUser.getUserRole()!=null&&orgUser.getUserRole().getRole().name().equalsIgnoreCase(roleName)) {
            organizationUserRepository.delete(orgUser);
        }
    }

}
