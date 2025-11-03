package com.example.issueservice.controller;

import com.example.issueservice.dto.*;
import com.example.issueservice.service.TicketService;
import com.its.commonservice.dto.StandardResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for ticket management endpoints
 * All responses return StandardResponse<T>
 */
@RestController
@RequestMapping("/api/orgs/{orgId}/projects/{projectId}/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    /**
     * Create a new ticket
     * POST /api/orgs/{orgId}/projects/{projectId}/tickets
     */
    @PostMapping
    public StandardResponse<TicketDTO> createTicket(
            @PathVariable Long orgId,
            @PathVariable Long projectId,
            @Valid @RequestBody CreateTicketRequestDTO request,
            @RequestAttribute("userId") Long userId) {
        
        TicketDTO ticket = ticketService.createTicket(orgId, projectId, request, userId);
        return StandardResponse.single(ticket, "Ticket created successfully");
    }

    /**
     * Get all tickets in a project with pagination
     * GET /api/orgs/{orgId}/projects/{projectId}/tickets
     */
    @GetMapping
    public StandardResponse<TicketDTO> getProjectTickets(
            @PathVariable Long orgId,
            @PathVariable Long projectId,
            Pageable pageable) {
        
        Page<TicketDTO> tickets = ticketService.getProjectTickets(projectId, pageable);
        return StandardResponse.page(tickets);
    }

    /**
     * Get ticket by ID
     * GET /api/orgs/{orgId}/tickets/{ticketId}
     */
    @GetMapping("/{ticketId}")
    public StandardResponse<TicketDTO> getTicket(@PathVariable Long ticketId) {
        TicketDTO ticket = ticketService.getTicketById(ticketId);
        return StandardResponse.single(ticket);
    }

    /**
     * Assign ticket to user or group
     * POST /api/orgs/{orgId}/tickets/{ticketId}/assign
     */
    @PostMapping("/{ticketId}/assign")
    public StandardResponse<Void> assignTicket(
            @PathVariable Long ticketId,
            @Valid @RequestBody AssignTicketRequestDTO request,
            @RequestAttribute("userId") Long userId) {
        
        ticketService.assignTicket(ticketId, request, userId);
        return StandardResponse.single(null, "Ticket assigned successfully");
    }

    /**
     * Update ticket status
     * POST /api/orgs/{orgId}/tickets/{ticketId}/status
     */
    @PostMapping("/{ticketId}/status")
    public StandardResponse<Void> updateStatus(
            @PathVariable Long ticketId,
            @Valid @RequestBody UpdateTicketStatusRequestDTO request,
            @RequestAttribute("userId") Long userId) {
        
        ticketService.updateTicketStatus(ticketId, request, userId);
        return StandardResponse.single(null, "Ticket status updated successfully");
    }

    /**
     * Add comment to ticket
     * POST /api/orgs/{orgId}/tickets/{ticketId}/comments
     */
    @PostMapping("/{ticketId}/comments")
    public StandardResponse<CommentDTO> addComment(
            @PathVariable Long ticketId,
            @Valid @RequestBody AddCommentRequestDTO request,
            @RequestAttribute("userId") Long userId) {
        
        CommentDTO comment = ticketService.addComment(ticketId, request, userId);
        return StandardResponse.single(comment, "Comment added successfully");
    }

    /**
     * Get ticket comments
     * GET /api/orgs/{orgId}/tickets/{ticketId}/comments
     */
    @GetMapping("/{ticketId}/comments")
    public StandardResponse<CommentDTO> getComments(@PathVariable Long ticketId) {
        List<CommentDTO> comments = ticketService.getTicketComments(ticketId);
        return StandardResponse.list(comments);
    }

    /**
     * Add internal work note
     * POST /api/orgs/{orgId}/tickets/{ticketId}/worknotes
     */
    @PostMapping("/{ticketId}/worknotes")
    public StandardResponse<WorkNoteDTO> addWorkNote(
            @PathVariable Long ticketId,
            @Valid @RequestBody AddWorkNoteRequestDTO request,
            @RequestAttribute("userId") Long userId) {
        WorkNoteDTO note = ticketService.addWorkNote(ticketId, request, userId);
        return StandardResponse.single(note, "Work note added successfully");
    }

    /**
     * Get work notes
     * GET /api/orgs/{orgId}/tickets/{ticketId}/worknotes
     */
    @GetMapping("/{ticketId}/worknotes")
    public StandardResponse<WorkNoteDTO> getWorkNotes(@PathVariable Long ticketId) {
        List<WorkNoteDTO> notes = ticketService.getWorkNotes(ticketId);
        return StandardResponse.list(notes);
    }

    /**
     * Get ticket history
     * GET /api/orgs/{orgId}/tickets/{ticketId}/history
     */
    @GetMapping("/{ticketId}/history")
    public StandardResponse<TicketHistoryDTO> getHistory(@PathVariable Long ticketId) {
        List<TicketHistoryDTO> history = ticketService.getTicketHistory(ticketId);
        return StandardResponse.list(history);
    }
}
