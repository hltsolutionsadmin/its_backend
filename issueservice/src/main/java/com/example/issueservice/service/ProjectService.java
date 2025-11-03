package com.example.issueservice.service;

import com.example.issueservice.dto.CreateProjectRequestDTO;
import com.example.issueservice.dto.ProjectDTO;
import com.example.issueservice.model.ProjectModel;
import com.example.issueservice.repository.ProjectRepository;
import com.example.issueservice.enums.ProjectStatus;
import com.example.issueservice.enums.ProjectType;
import com.example.issueservice.utils.ProjectCodeGenerator;
import com.its.commonservice.exception.ErrorCode;
import com.its.commonservice.exception.HltCustomerException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Service for project management operations
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectService {
    private final ProjectRepository projectRepository;

    private final ProjectCodeGenerator ProjectCodeGenerator;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public ProjectDTO createProject(Long orgId, CreateProjectRequestDTO request) {
        log.info("Creating project. Provided code: {}", request.getProjectCode());

        String code = request.getProjectCode();
        if (code == null || code.isBlank()) {
            code = generateUniqueProjectCode(request.getName());
        }
        code = code.toUpperCase(Locale.ROOT);
        if (projectRepository.existsByProjectCode(code)) {
            throw new HltCustomerException(ErrorCode.PROJECT_CODE_TAKEN);
        }

        ProjectModel project = new ProjectModel();
        project.setOrganizationId(orgId);
        project.setName(request.getName());
        project.setProjectCode(ProjectCodeGenerator.generateCode(request.getName()));
        project.setDescription(request.getDescription());
        project.setManagerId(request.getManagerId());
        project.setActive(true);
        // Optional new fields
        // Default sensible values if null
        project.setStatus(request.getStatus() != null ? request.getStatus() : ProjectStatus.PLANNED);
        project.setType(request.getType());
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());
        project.setTargetEndDate(request.getTargetEndDate());
        project.setDueDate(request.getDueDate());
        project.setOwnerOrganizationId(request.getOwnerOrganizationId());
        project.setClientOrganizationId(request.getClientOrganizationId());
        project.setClientId(request.getClientId());
        project.setProgressPercentage(request.getProgressPercentage());

        project = projectRepository.save(project);

        log.info("Project created successfully with ID: {}", project.getId());

        return buildProjectDTO(project);
    }

    @Transactional(readOnly = true)
    public ProjectDTO getProjectById(Long projectId) {
        ProjectModel project = projectRepository.findById(projectId)
            .orElseThrow(() -> new HltCustomerException(ErrorCode.PROJECT_NOT_FOUND));
        
        return buildProjectDTO(project);
    }

    @Transactional(readOnly = true)
    public Page<ProjectDTO> getOrganizationProjects(Long orgId, Pageable pageable) {
        return projectRepository.findByOrganizationId(orgId, pageable)
                .map(this::buildProjectDTO);
    }

    @Transactional(readOnly = true)
    public Page<ProjectDTO> searchProjects(Long orgId, String search, Pageable pageable) {
        return projectRepository.searchByOrganization(orgId, search, pageable)
                .map(this::buildProjectDTO);
    }

    @Transactional(readOnly = true)
    public Page<ProjectDTO> filterProjects(Long orgId,
                                           String search,
                                           ProjectStatus status,
                                           ProjectType type,
                                           Long managerId,
                                           Boolean active,
                                           LocalDate startDateFrom,
                                           LocalDate startDateTo,
                                           Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<ProjectModel> cq = cb.createQuery(ProjectModel.class);
        Root<ProjectModel> root = cq.from(ProjectModel.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(root.get("organizationId"), orgId));
        if (search != null && !search.isBlank()) {
            String like = "%" + search.toLowerCase(Locale.ROOT) + "%";
            predicates.add(cb.or(
                    cb.like(cb.lower(root.get("name")), like),
                    cb.like(cb.lower(root.get("projectCode")), like)
            ));
        }
        if (status != null) {
            predicates.add(cb.equal(root.get("status"), status));
        }
        if (type != null) {
            predicates.add(cb.equal(root.get("type"), type));
        }
        if (managerId != null) {
            predicates.add(cb.equal(root.get("managerId"), managerId));
        }
        if (active != null) {
            predicates.add(cb.equal(root.get("active"), active));
        }
        if (startDateFrom != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("startDate"), startDateFrom));
        }
        if (startDateTo != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("startDate"), startDateTo));
        }

        cq.where(predicates.toArray(new Predicate[0]));
        // order by createdAt desc by default
        cq.orderBy(cb.desc(root.get("createdAt")));

        var query = entityManager.createQuery(cq);
        // pagination
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        List<ProjectModel> results = query.getResultList();

        // total count
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<ProjectModel> countRoot = countQuery.from(ProjectModel.class);
        countQuery.select(cb.count(countRoot)).where(predicates.toArray(new Predicate[0]));
        Long total = entityManager.createQuery(countQuery).getSingleResult();

        return new org.springframework.data.domain.PageImpl<>(
                results.stream().map(this::buildProjectDTO).toList(),
                pageable,
                total
        );
    }

    private String generateUniqueProjectCode(String name) {
        String base = name == null ? "PROJ" : name.replaceAll("[^A-Za-z0-9]+", " ").trim();
        if (base.isBlank()) base = "PROJ";
        String[] parts = base.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (!p.isBlank()) sb.append(Character.toUpperCase(p.charAt(0)));
            if (sb.length() >= 4) break;
        }
        if (sb.length() < 2) {
            sb = new StringBuilder(base.substring(0, Math.min(4, base.length())));
        }
        String candidate = sb.toString().toUpperCase(Locale.ROOT);
        if (candidate.length() < 2) candidate = "PR";
        int suffix = 1;
        String code = candidate;
        while (projectRepository.existsByProjectCode(code)) {
            code = candidate + String.format("%02d", suffix++);
        }
        return code;
    }

    @Transactional
    public ProjectDTO updateProject(Long projectId, CreateProjectRequestDTO request) {
        log.info("Updating project with ID: {}", projectId);
        
        ProjectModel project = projectRepository.findById(projectId)
            .orElseThrow(() -> new HltCustomerException(ErrorCode.PROJECT_NOT_FOUND));
        
        if (request.getName() != null) {
            project.setName(request.getName());
        }
        if (request.getDescription() != null) {
            project.setDescription(request.getDescription());
        }
        if (request.getManagerId() != null) {
            project.setManagerId(request.getManagerId());
        }
        
        project = projectRepository.save(project);
        
        log.info("Project updated successfully: {}", projectId);
        
        return buildProjectDTO(project);
    }

    @Transactional
    public void deactivateProject(Long projectId) {
        log.info("Deactivating project with ID: {}", projectId);
        
        ProjectModel project = projectRepository.findById(projectId)
            .orElseThrow(() -> new HltCustomerException(ErrorCode.PROJECT_NOT_FOUND));
        
        project.setActive(false);
        projectRepository.save(project);
        
        log.info("Project deactivated successfully: {}", projectId);
    }

    private ProjectDTO buildProjectDTO(ProjectModel project) {
        return ProjectDTO.builder()
            .id(project.getId())
            .organizationId(project.getOrganizationId())
            .name(project.getName())
            .projectCode(project.getProjectCode())
            .description(project.getDescription())
            .managerId(project.getManagerId())
            .active(project.getActive())
            .createdAt(project.getCreatedAt())
            .updatedAt(project.getUpdatedAt())
            .memberCount(project.getMembers() != null ? project.getMembers().size() : 0)
            .ticketCount(project.getTickets() != null ? project.getTickets().size() : 0)
            .build();
    }
}
