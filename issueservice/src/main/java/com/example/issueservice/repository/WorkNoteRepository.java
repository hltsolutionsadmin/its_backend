package com.example.issueservice.repository;

import com.example.issueservice.model.WorkNoteModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkNoteRepository extends JpaRepository<WorkNoteModel, Long> {
    List<WorkNoteModel> findByTicketIdOrderByCreatedAtAsc(Long ticketId);
}
