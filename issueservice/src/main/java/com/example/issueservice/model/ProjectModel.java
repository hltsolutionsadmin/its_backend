package com.example.issueservice.model;

import com.its.commonservice.enums.ProjectStatus;
import com.its.commonservice.enums.ProjectType;
import com.its.commonservice.enums.SlaTier;
import com.its.commonservice.enums.GenericModel;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Where;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "PROJECTS", indexes = {
        @Index(name = "idx_project_name", columnList = "NAME"),
        @Index(name = "idx_project_status", columnList = "STATUS"),
        @Index(name = "idx_project_sla", columnList = "SLA_TIER")
})
@Getter
@Setter
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@ToString(callSuper = true, exclude = {"tickets", "technologyStack", "userAssignments"})
public class ProjectModel extends GenericModel {

    @Column(name = "NAME", nullable = false)
    @EqualsAndHashCode.Include
    private String name;

    @Column(name = "PROJECT_CODE", unique = true, length = 10)
    private String projectCode;

    @Column(name = "DESCRIPTION", length = 2000)
    private String description;

    @Column(name = "CLIENT_ID", nullable = true)
    private Long clientId;

    @Column(name = "PROJECT_MANAGER_ID")
    private Long projectManagerId;

    @Column(name = "START_DATE")
    private LocalDate startDate;

    @Column(name = "END_DATE")
    private LocalDate endDate;

    @Column(name = "TARGET_END_DATE")
    private LocalDate targetEndDate;

    @Column(name = "DUE_DATE")
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = true)
    private ProjectStatus status = ProjectStatus.PLANNED;

    @Enumerated(EnumType.STRING)
    @Column(name = "TYPE")
    private ProjectType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "SLA_TIER")
    private SlaTier slaTier;

    @Column(name = "OWNER_ORG_ID", nullable = true)
    private Long ownerOrganizationId;

    @Column(name = "CLIENT_ORG_ID")
    private Long clientOrganizationId;

    @Column(name = "TICKET_IDS")
    private List<Long> ticketsId = new ArrayList<>();

    @Column(name = "USER_ASSIGNMENT_IDS")
    private List<Long> userAssignmentIds = new ArrayList<>();

    @Column(name = "TECH_STACK_IDS")
    private List<Long> technologyStackIds = new ArrayList<>();

    @Column(nullable = true)
    private int progressPercentage = 0;

    @Column(name = "BUDGET_RANGE")
    private String budgetRange;

    @Column(name = "EXPECTED_TEAM_SIZE")
    private String expectedTeamSize;

    @Column(name = "ARCHIVED", nullable = false)
    private Boolean archived = false;

    @Column(name = "USER_GROUPS_IDS")
    private List<Long> userGroupIds = new ArrayList<>();

}