package com.example.issueservice.controller;

import com.its.common.dto.TicketCommentDTO;
import com.its.common.dto.TicketDTO;
import com.example.issueservice.service.TicketService;
import com.its.commonservice.dto.StandardResponse;
import com.its.commonservice.enums.TicketStatus;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/tickets")
@AllArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @PostMapping
    public StandardResponse<TicketDTO> createTicket( @RequestBody TicketDTO ticketDTO) {
        TicketDTO created = ticketService.createOrUpdateTicket(ticketDTO);
        return StandardResponse.single(created,"Ticket created successfully" );
    }

    @GetMapping("/{id}")
    public StandardResponse<TicketDTO> getTicket(@PathVariable Long id) {
        TicketDTO ticket = ticketService.getTicketById(id);
        return StandardResponse.single(ticket,"Ticket fetched successfully");
    }

    @GetMapping
    public StandardResponse<TicketDTO> getAllTickets(
            Pageable pageable,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority
    ) {
        Page<TicketDTO> tickets = ticketService.getAllTickets(pageable, projectId, status, priority);
        return StandardResponse.page( tickets);
    }


    @DeleteMapping("/{id}")
    public StandardResponse<Void> deleteTicket(@PathVariable Long id) {
        ticketService.deleteTicket(id);
        return StandardResponse.message("Ticket deleted successfully");
    }

    @PostMapping("/{id}/comments")
    public StandardResponse<TicketCommentDTO> addComment(
            @PathVariable Long id,
            @Valid @RequestBody TicketCommentDTO commentDTO
    ) {
        TicketCommentDTO added = ticketService.addComment(id, commentDTO);
        return StandardResponse.single(added,"Comment added successfully");
    }


    @PostMapping("/{ticketId}/assign/{assigneeId}")
    public StandardResponse<TicketDTO> assignTicket(
            @PathVariable Long ticketId,
            @PathVariable Long assigneeId) {
        TicketDTO updated = ticketService.assignTicket(ticketId, assigneeId);
        return StandardResponse.single(updated,"Ticket assigned successfully");
    }

    @PatchMapping("/{ticketId}/status")
    public StandardResponse<TicketDTO> updateTicketStatus(
            @PathVariable Long ticketId,
            @RequestParam TicketStatus status) {
        TicketDTO updated = ticketService.updateTicketStatus(ticketId, status);
        return StandardResponse.single(updated,"Ticket status updated successfully");
    }



}
