package com.its.userservice.service.impl;

import com.its.userservice.dto.ProvisionOrganizationRequestDTO;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProvisioningService {

    private final OrganizationRepository organizationRepository;
    private final OrganizationUserRepository organizationUserRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OrganizationPopulator organizationPopulator;
    private final RoleService roleService;

    private static final Pattern NON_ALNUM = Pattern.compile("[^A-Z0-9_]");

    @Transactional
    public OrganizationDTO provisionOrganization(ProvisionOrganizationRequestDTO request) {
        log.info("Provisioning organization for domain: {}", request.getDomainName());

        // Validate uniqueness of admin email/username
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new HltCustomerException(ErrorCode.USER_ALREADY_EXISTS, "Admin email already in use");
        }
        if (userRepository.existsByUsername(request.getAdminUsername())) {
            throw new HltCustomerException(ErrorCode.USER_ALREADY_EXISTS, "Admin username already in use");
        }

        // Create admin user
        UserModel admin = new UserModel();
        admin.setEmail(request.getEmail());
        admin.setUsername(request.getAdminUsername());
        admin.setPasswordHash(passwordEncoder.encode(request.getAdminPassword()));
        admin.setPhone(request.getAdminPrimaryContact());
        admin.setActive(request.getActive() == null ? true : request.getActive());
        admin.setEmailVerified(false);
        // Split full name
        String[] parts = request.getAdminFullName().trim().split(" ", 2);
        admin.setFirstName(parts[0]);
        admin.setLastName(parts.length > 1 ? parts[1] : "");
        admin = userRepository.save(admin);

        // Build organization
        OrganizationModel org = new OrganizationModel();
        org.setName(request.getName());
        String generatedCode = generateUniqueOrgCode(request.getDomainName(), request.getName());
        org.setOrgCode(generatedCode);
        org.setDescription(request.getDescription());
        org.setWebsite("https://" + request.getDomainName());
        org.setActive(request.getActive() == null ? true : request.getActive());
        org.setOwner(admin);
        org = organizationRepository.save(org);

        // Link admin as ORG_ADMIN
        OrganizationUserModel orgUser = new OrganizationUserModel();
        orgUser.setOrganization(org);
        orgUser.setUser(admin);
        orgUser.setUserRole(roleService.getByName(UserRole.ORG_ADMIN));
        orgUser.setActive(true);
        orgUser.setInvitedBy(admin);
        organizationUserRepository.save(orgUser);

        log.info("Provisioned organization {} with code {}", org.getId(), org.getOrgCode());
        return organizationPopulator.populate(org);
    }

    private String generateUniqueOrgCode(String domainName, String orgName) {
        String base = (domainName != null && !domainName.isBlank())
            ? domainName.split("\\.")[0]
            : orgName;
        String upper = NON_ALNUM.matcher(base.toUpperCase(Locale.ROOT).replace('-', '_')).replaceAll("");
        if (upper.length() > 20) {
            upper = upper.substring(0, 20);
        }
        if (upper.length() < 2) {
            upper = (upper + "ORG").substring(0, Math.min(20, (upper + "ORG").length()));
        }
        String candidate = upper;
        int suffix = 1;
        while (organizationRepository.existsByOrgCode(candidate)) {
            String s = String.valueOf(suffix++);
            int maxBase = Math.max(2, 20 - s.length());
            candidate = upper.substring(0, Math.min(upper.length(), maxBase)) + s;
        }
        return candidate;
    }
}
