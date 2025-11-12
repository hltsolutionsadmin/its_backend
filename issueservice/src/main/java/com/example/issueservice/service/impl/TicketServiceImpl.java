package com.example.issueservice.service.impl;

import com.its.common.dto.TicketCommentDTO;
import com.its.common.dto.TicketDTO;
import com.its.common.dto.UserDTO;
import com.its.common.dto.UserGroupDTO;
import com.example.issueservice.model.ProjectModel;
import com.example.issueservice.model.TicketCommentModel;
import com.example.issueservice.model.TicketModel;
import com.example.issueservice.populator.TicketCommentPopulator;
import com.example.issueservice.populator.TicketPopulator;
import com.example.issueservice.repository.ProjectRepository;
import com.example.issueservice.repository.TicketCommentRepository;
import com.example.issueservice.repository.TicketRepository;
import com.example.issueservice.service.TicketService;
import com.example.issueservice.client.UserGroupClient;
import com.example.issueservice.client.UserClient;

import com.its.commonservice.enums.TicketPriority;
import com.its.commonservice.enums.TicketStatus;
import com.its.commonservice.exception.ErrorCode;
import com.its.commonservice.exception.HltCustomerException;
import com.its.commonservice.util.CurrentUserUtil;
import com.its.commonservice.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final ProjectRepository projectRepository;
    private final TicketCommentRepository ticketCommentRepository;
    private final UserGroupClient userGroupClient;
    private final UserClient userClient;

    private final TicketPopulator ticketPopulator;
    private final TicketCommentPopulator ticketCommentPopulator;


    @Override
    public TicketDTO createOrUpdateTicket(TicketDTO ticketDTO) {
        final TicketModel ticketModel = ticketDTO.getId() != null
                ? ticketRepository.findById(ticketDTO.getId())
                .orElseThrow(() -> new HltCustomerException(ErrorCode.TICKET_NOT_FOUND))
                : new TicketModel();

        applyDtoToModel(ticketDTO, ticketModel);

        if (ticketModel.getId() == null) {
            generateTicketIdIfNew(ticketModel);
        }

        if (ticketModel.getPriority() == TicketPriority.HIGH && ticketModel.getAssignedToId() == null) {
            autoAssignHighPriorityTicket(ticketModel);
        }

        TicketModel saved = ticketRepository.save(ticketModel);
        return ticketPopulator.toDTO(saved);
    }

    @Override
    public TicketDTO getTicketById(Long ticketId) {
        TicketModel model = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new HltCustomerException(ErrorCode.TICKET_NOT_FOUND));
        return ticketPopulator.toDTO(model);
    }

    @Override
    public Page<TicketDTO> getAllTickets(Pageable pageable, Long projectId, String statusStr, String priorityStr) {
        Page<TicketModel> tickets = fetchTicketsWithFilters(pageable, projectId, statusStr, priorityStr);

        List<TicketDTO> dtoList = tickets.getContent().stream()
                .map(ticketPopulator::toDTO)
                .collect(Collectors.toList());

        return new PageImpl<>(dtoList, pageable, tickets.getTotalElements());
    }

    @Override
    public void deleteTicket(Long ticketId) {
        TicketModel ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new HltCustomerException(ErrorCode.TICKET_NOT_FOUND));
        ticketRepository.delete(ticket);
    }

    @Override
    public TicketCommentDTO addComment(Long ticketId, TicketCommentDTO commentDTO) {
        TicketModel ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new HltCustomerException(ErrorCode.TICKET_NOT_FOUND));

        UserDTO CreatedBy = userClient.getUserByEmail(SecurityUtils.getCurrentUser()).getData();
        if (CreatedBy == null || CreatedBy.getId() == null) {
            throw new HltCustomerException(ErrorCode.USER_NOT_FOUND);
        }

        TicketCommentModel comment = new TicketCommentModel();
        comment.setTicket(ticket);
        comment.setComment(commentDTO.getComment());
        comment.setCreatedBy(CreatedBy.getId());

        TicketCommentModel saved = ticketCommentRepository.save(comment);
        TicketCommentDTO resultDTO = new TicketCommentDTO();
        ticketCommentPopulator.populate(saved, resultDTO);
        return resultDTO;
    }


    @Override
    public TicketDTO assignTicket(Long ticketId, Long assigneeId) {
        TicketModel ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new HltCustomerException(ErrorCode.TICKET_NOT_FOUND));

        // Already assigned validation
        if (ticket.getAssignedToId() != null && ticket.getAssignedToId().equals(assigneeId)) {
            throw new HltCustomerException(ErrorCode.TICKET_ALREADY_ASSIGNED);
        }
        UserDTO assignedTo = userClient.getUserById(assigneeId).getData();
        if (assignedTo == null || assignedTo.getId() == null) {
            throw new HltCustomerException(ErrorCode.USER_NOT_FOUND);
        }
        // Role-based assignment check (optional)
        // validateAssignmentPermissions(SecurityUtils.getCurrentUserDetails(), ticket);

        ticket.setAssignedToId(assignedTo.getId());
        ticket.setStatus(TicketStatus.ASSIGNED);
        return ticketPopulator.toDTO(ticketRepository.save(ticket));
    }


    @Override
    public TicketDTO updateTicketStatus(Long ticketId, TicketStatus status) {
        TicketModel ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new HltCustomerException(ErrorCode.TICKET_NOT_FOUND));

//        Long currentUserId = SecurityUtils.getCurrentUserDetails().getId();
//
//        if (ticket.getAssignedTo() == null || !ticket.getAssignedTo().getId().equals(currentUserId)) {
//            throw new HltCustomerException(ErrorCode.UNAUTHORIZED, "You are not allowed to update this ticket status");
//        }

        if (ticket.getStatus() == TicketStatus.CLOSED) {
            throw new HltCustomerException(ErrorCode.TICKET_ALREADY_CLOSED, "Cannot update a closed ticket");
        }

        ticket.setStatus(status);
        return ticketPopulator.toDTO(ticketRepository.save(ticket));
    }

    private void autoAssignHighPriorityTicket(TicketModel ticket) {
        // Fetch groups by project and current ticket status via userservice
        var response = userGroupClient.getGroupsByProjectAndStatus(
                ticket.getProject().getId(),
                ticket.getStatus() != null ? ticket.getStatus() : TicketStatus.OPEN
                );

        UserGroupDTO groupDto = (response != null) ? response.getData() : null;
        if (groupDto == null) {
            throw new HltCustomerException(ErrorCode.GROUP_NOT_FOUND_FOR_PROJECT);
        }
        if (groupDto.getGroupLead() == null || groupDto.getGroupLead().getId() == null) {
            throw new HltCustomerException(ErrorCode.GROUP_LEAD_NOT_ASSIGNED);
        }
        ticket.setAssignedToId(groupDto.getGroupLead().getId());
        ticket.setStatus(TicketStatus.ASSIGNED);
    }

    private Page<TicketModel> fetchTicketsWithFilters(Pageable pageable, Long projectId, String statusStr, String priorityStr) {
        TicketStatus status = parseEnum(statusStr, TicketStatus.class, "Invalid ticket status");
        TicketPriority priority = parseEnum(priorityStr, TicketPriority.class, "Invalid ticket priority");

        if (projectId != null && status != null && priority != null)
            return ticketRepository.findByProjectIdAndStatusAndPriority(projectId, status, priority, pageable);
        if (projectId != null && status != null)
            return ticketRepository.findByProjectIdAndStatus(projectId, status, pageable);
        if (projectId != null && priority != null)
            return ticketRepository.findByProjectIdAndPriority(projectId, priority, pageable);
        if (status != null && priority != null)
            return ticketRepository.findByStatusAndPriority(status, priority, pageable);
        if (projectId != null)
            return ticketRepository.findByProjectId(projectId, pageable);
        if (status != null)
            return ticketRepository.findByStatus(status, pageable);
        if (priority != null)
            return ticketRepository.findByPriority(priority, pageable);

        return ticketRepository.findAll(pageable);
    }

    private <E extends Enum<E>> E parseEnum(String value, Class<E> enumType, String errorMsg) {
        if (value == null) return null;
        try {
            return Enum.valueOf(enumType, value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new HltCustomerException(ErrorCode.BUSINESS_VALIDATION_FAILED, errorMsg + ": " + value);
        }
    }
    private void applyDtoToModel(TicketDTO dto, TicketModel model) {

        updateIfPresent(dto.getTitle(), model::setTitle);
        updateIfPresent(dto.getDescription(), model::setDescription);
        updateIfPresent(dto.getDueDate(), model::setDueDate);
        updateIfPresent(dto.getArchived(), model::setArchived);

        if (dto.getStatus() != null) {
            model.setStatus(dto.getStatus());
        } else if (model.getId() == null) {
            model.setStatus(TicketStatus.OPEN);
        }

        if (dto.getPriority() != null) {
            model.setPriority(dto.getPriority());
        } else if (model.getId() == null) {
            model.setPriority(TicketPriority.LOW);
        }

        if (dto.getProjectId() != null) {
            ProjectModel project = projectRepository.findById(dto.getProjectId())
                    .orElseThrow(() -> new HltCustomerException(ErrorCode.PROJECT_NOT_FOUND));
            model.setProject(project);
        }

        if (model.getId() == null) {

            UserDTO CreatedBy = userClient.getUserByEmail(SecurityUtils.getCurrentUser()).getData();
            if (CreatedBy == null || CreatedBy.getId() == null) {
                throw new HltCustomerException(ErrorCode.USER_NOT_FOUND);
            }
            model.setCreatedById(CreatedBy.getId());
        }

        if (dto.getAssignedToId() != null) {
            UserDTO assigned = userClient.getUserById(dto.getAssignedToId()).getData();
            if (assigned == null || assigned.getId() == null) {
                throw new HltCustomerException(ErrorCode.USER_NOT_FOUND);
            }
            model.setAssignedToId(assigned.getId());
        }

        if (dto.getUserGroupDTO() != null && dto.getUserGroupDTO().getId() != null) {
            var groupResp = userGroupClient.getById(dto.getUserGroupDTO().getId());
            var groupDto = groupResp != null ? groupResp.getData() : null;
            if (groupDto == null) {
                throw new HltCustomerException(ErrorCode.GROUP_NOT_FOUND);
            }
            model.setGroupId(groupDto.getId());
        }
    }

    private <T> void updateIfPresent(T value, java.util.function.Consumer<T> setter) {
        if (value != null) {
            setter.accept(value);
        }
    }

    private void generateTicketIdIfNew(TicketModel model) {
        if (model.getId() != null) return;

        Long projectId = model.getProject().getId();

        Long lastNumber = ticketRepository.getLastTicketNumberByProject(projectId);
        Long nextNumber = lastNumber + 1;

        String projectKey = getProjectKey(model.getProject());
        String ticketId = projectKey + "-" + nextNumber;

        model.setTicketNumber(nextNumber);
        model.setTicketId(ticketId);
    }

    private String getProjectKey(ProjectModel project) {
        String name = project.getName();
        return name.replaceAll("[^A-Za-z]", "")
                .substring(0, Math.min(4, name.length()))
                .toUpperCase();
    }


}
