package com.example.issueservice.service.impl;


import com.example.issueservice.client.UserClient;
import com.example.issueservice.model.ProjectModel;
import com.example.issueservice.populator.ProjectPopulator;
import com.example.issueservice.repository.ProjectRepository;
import com.example.issueservice.service.ProjectService;
import com.example.issueservice.utils.ProjectCodeGenerator;
import com.its.common.dto.ProjectDTO;
import com.its.common.dto.UserDTO;
import com.its.common.dto.ProjectStatsDTO;
import com.its.commonservice.enums.ProjectStatus;
import com.its.commonservice.enums.SlaTier;
import com.its.commonservice.exception.ErrorCode;
import com.its.commonservice.exception.HltCustomerException;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@AllArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectPopulator projectPopulator;
    private final ProjectCodeGenerator projectCodeGenerator;
    private final UserClient userClient;


    @Override
    @Transactional
    public ProjectDTO saveOrUpdateProject(ProjectDTO projectDTO) {
        ProjectModel model;

        if (projectDTO.getId() == null) {
            model = new ProjectModel();

            model.setProjectCode(generateUniqueProjectCode(projectDTO.getName()));

        } else {
            model = projectRepository.findById(projectDTO.getId())
                    .orElseThrow(() -> new HltCustomerException(ErrorCode.PROJECT_NOT_FOUND));

            if (projectDTO.getProjectCode() != null
                    && !model.getProjectCode().equals(projectDTO.getProjectCode())
                    && projectRepository.existsByProjectCode(projectDTO.getProjectCode())) {
                throw new HltCustomerException(ErrorCode.PROJECT_ALREADY_REGISTERED);
            }
        }

        mapDtoToModel(projectDTO, model);

        ProjectModel saved = projectRepository.save(model);

        return projectPopulator.toDTO(saved);
    }




    @Override
    public ProjectDTO getProjectById(Long projectId) {
        ProjectModel model = projectRepository.findById(projectId)
                .orElseThrow(() -> new HltCustomerException(ErrorCode.BUSINESS_NOT_FOUND));
        return projectPopulator.toDTO(model);
    }

    @Override
    public Page<ProjectDTO> fetchProjectsWithFilters(Pageable pageable, Long projectId, Long clientId, Long managerId, String status) {
        Page<ProjectModel> page = fetchProjectsWithFilter(pageable, projectId, clientId, managerId, status);
        List<ProjectDTO> dtos = page.getContent().stream()
                .map(projectPopulator::toDTO)
                .collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, page.getTotalElements());
    }


    @Override
    public void deleteProject(Long projectId) {
        ProjectModel model = projectRepository.findById(projectId)
                .orElseThrow(() -> new HltCustomerException(ErrorCode.BUSINESS_NOT_FOUND));
        projectRepository.delete(model);
    }

    @Override
    public Page<ProjectDTO> getProjectsForOrganization(Long organizationId, Pageable pageable) {
        Page<ProjectModel> projectsPage = projectRepository.findByOrganization(organizationId, pageable);

        List<ProjectDTO> dtos = projectsPage.getContent().stream()
                .map(projectPopulator::toDTO)
                .collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, projectsPage.getTotalElements());
    }

    @Override
    public ProjectStatsDTO getProjectStats() {
        return ProjectStatsDTO.builder()
                .totalProjects(projectRepository.countAllProjects())
                .activeProjects(projectRepository.countActiveProjects())
                .completedProjects(projectRepository.countCompletedProjects())
                .onHoldProjects(projectRepository.countOnHoldProjects())
                .build();
    }


    private ProjectModel mapDtoToModel(ProjectDTO dto, ProjectModel model) {

        if (dto.getName() != null) model.setName(dto.getName());
        if (dto.getDescription() != null) model.setDescription(dto.getDescription());
        if (dto.getStartDate() != null) model.setStartDate(dto.getStartDate());
        if (dto.getEndDate() != null) model.setEndDate(dto.getEndDate());
        if (dto.getTargetEndDate() != null) model.setTargetEndDate(dto.getTargetEndDate());
        if (dto.getDueDate() != null) model.setDueDate(dto.getDueDate());
        if (dto.getStatus() != null) model.setStatus(dto.getStatus());
        if (dto.getType() != null) model.setType(dto.getType());
        if (dto.getSlaTier() != null) {
            model.setSlaTier(dto.getSlaTier());
        } else if (model.getId() == null) {
            model.setSlaTier(SlaTier.STANDARD);
        }
        if (dto.getProgressPercentage() != null) model.setProgressPercentage(dto.getProgressPercentage());
        if (dto.getBudgetRange() != null) model.setBudgetRange(dto.getBudgetRange());
        if (dto.getExpectedTeamSize() != null) model.setExpectedTeamSize(dto.getExpectedTeamSize());
        if (dto.getArchived() != null) model.setArchived(dto.getArchived());

        if (dto.getClientId() != null) {
            model.setClientId(dto.getClientId());
        } else if (dto.getClientUsername() != null && dto.getCleintFullName() != null) {
            Long ClientId = registerNewClient(dto);
            model.setClientId(ClientId);

        }

        if (dto.getProjectManagerId() != null) {
            model.setProjectManagerId(dto.getProjectManagerId());
        }

        if (dto.getOwnerOrganizationId() != null)
            model.setOwnerOrganizationId(dto.getOwnerOrganizationId());

        if (dto.getClientOrganizationId() != null)
            model.setClientOrganizationId(dto.getClientOrganizationId());

        return model;
    }

    private Long registerNewClient(ProjectDTO dto) {
        UserDTO req = UserDTO.builder()
                .firstName(dto.getCleintFullName())
                .username(dto.getClientEmail())
                .email(dto.getClientEmail())
                .password(dto.getCleintPassword())
                .build();

        UserDTO created = userClient.saveUser(req).getData();
        if (created == null || created.getId() == null) {
            throw new HltCustomerException(ErrorCode.USER_CREATION_FAILED);
        }
        return created.getId();
    }



    private Page<ProjectModel> fetchProjectsWithFilter(Pageable pageable, Long organisationId, Long clientId, Long managerId, String statusStr) {
        ProjectStatus status = null;
        if (statusStr != null) {
            try {
                status = ProjectStatus.valueOf(statusStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new HltCustomerException(ErrorCode.BUSINESS_VALIDATION_FAILED, "Invalid project status: " + statusStr);
            }
        }

        if (clientId != null && managerId != null && status != null) {
            return projectRepository.findByClientIdAndProjectManagerIdAndStatus(
                    clientId, managerId, status, pageable);
        } else if (clientId != null && managerId != null) {
            return projectRepository.findByClientIdAndProjectManagerId(clientId, managerId, pageable);
        } else if (clientId != null && status != null) {
            return projectRepository.findByClientIdAndStatus(clientId, status, pageable);
        } else if (managerId != null && status != null) {
            return projectRepository.findByProjectManagerIdAndStatus(managerId, status, pageable);
        } else if (clientId != null) {
            return projectRepository.findByClientId(clientId, pageable);
        } else if (managerId != null) {
            return projectRepository.findByProjectManagerId(managerId, pageable);
        } else if (status != null) {
            return projectRepository.findByOwnerOrganizationIdAndStatus(organisationId,status, pageable);
        } else {
            return projectRepository.findAll(pageable);
        }
    }


    private String generateUniqueProjectCode(String projectName) {
        final int MAX_RETRIES = 5;

        for (int i = 0; i < MAX_RETRIES; i++) {
            String candidate = projectCodeGenerator.generateCode(projectName);
            boolean exists = projectRepository.existsByProjectCode(candidate);

            if (!exists) {
                return candidate;
            }
        }

        throw new HltCustomerException(ErrorCode.PROJECT_CODE_GENERATION_FAILED);
    }


}
