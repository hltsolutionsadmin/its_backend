package com.example.issueservice.populator;

import com.example.issueservice.model.TicketModel;
import com.its.common.dto.TicketCommentDTO;
import com.its.common.dto.TicketDTO;
import com.its.common.dto.UserGroupDTO;
import com.its.common.populator.Populator;
import com.example.issueservice.client.UserServiceClient;
import com.example.issueservice.repository.TicketCommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class TicketPopulator implements Populator<TicketModel, TicketDTO> {
    private final TicketCommentPopulator commentPopulator;
    private final UserServiceClient userServiceClient;
    private final TicketCommentRepository ticketCommentRepository;

    @Autowired
    public TicketPopulator(TicketCommentPopulator commentPopulator,
                           UserServiceClient userServiceClient,
                           TicketCommentRepository ticketCommentRepository) {
        this.commentPopulator = commentPopulator;
        this.userServiceClient = userServiceClient;
        this.ticketCommentRepository = ticketCommentRepository;
    }

    @Override
    public void populate(TicketModel source, TicketDTO target) {
        if (source == null || target == null) {
            return;
        }

        target.setId(source.getId());
        target.setTicketId(source.getTicketId());
//        target.setTicketNumber(source.getTicketNumber());
        target.setTitle(source.getTitle());
        target.setDescription(source.getDescription());
        target.setPriority(source.getPriority());
        target.setStatus(source.getStatus());
        target.setDueDate(source.getDueDate());
        target.setResolvedAt(source.getResolvedAt());
        target.setArchived(source.getArchived());

        if (source.getProject() != null) {
            target.setProjectId(source.getProject().getId());
        }

        if (source.getGroupId() != null) {
            try {
                UserGroupDTO groupDTO = userServiceClient.getUserGroupById(source.getGroupId()).getData();
                target.setUserGroupDTO(groupDTO);
            } catch (Exception ex) {
                    UserGroupDTO groupDTO = new UserGroupDTO();
                    target.setUserGroupDTO(groupDTO);
            }
        }

        if (source.getCreatedById() != null) {
            target.setCreatedById(source.getCreatedById());
        }

        if (source.getAssignedToId() != null) {
            target.setAssignedToId(source.getAssignedToId() );
        }

        target.setComments(
                ticketCommentRepository.findByTicketIdOrderByCreatedAtAsc(source.getId())
                        .stream()
                        .map(comment -> {
                            TicketCommentDTO dto = new TicketCommentDTO();
                            commentPopulator.populate(comment, dto);
                            return dto;
                        })
                        .collect(Collectors.toList())
        );
    }

    public TicketDTO toDTO(TicketModel source) {
        TicketDTO dto = new TicketDTO();
        populate(source, dto);
        return dto;
    }
}
