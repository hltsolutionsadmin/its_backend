package com.example.issueservice.service;

import com.its.common.dto.TicketCommentDTO;
import com.its.common.dto.TicketDTO;
import com.its.commonservice.enums.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TicketService {

    TicketDTO createOrUpdateTicket(TicketDTO ticketDTO);

    TicketDTO getTicketById(Long ticketId);

    Page<TicketDTO> getAllTickets(Pageable pageable, Long projectId, String status, String priority);

    void deleteTicket(Long ticketId);

    TicketCommentDTO addComment(Long ticketId, TicketCommentDTO commentDTO);

    TicketDTO assignTicket(Long ticketId, Long assigneeId);


    TicketDTO updateTicketStatus(Long ticketId, TicketStatus status);
}
