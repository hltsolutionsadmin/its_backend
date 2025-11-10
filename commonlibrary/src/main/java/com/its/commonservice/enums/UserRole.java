package com.its.commonservice.enums;

/**
 * User roles in the system
 * SUPER_ADMIN: Global admin across all organizations
 * ORG_ADMIN: Organization administrator
 * PROJECT_MANAGER: Can manage projects and teams
 * MEMBER: Regular member (can create tickets, comment, view assigned items)
 * CLIENT: External client who can create service requests
 */

public enum UserRole {
    SUPER_ADMIN,
    ORG_ADMIN,
    PROJECT_MANAGER,
    MEMBER,
    CLIENT,
    ORG_MEMBER
}

