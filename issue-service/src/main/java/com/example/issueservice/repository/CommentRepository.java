package com.example.issueservice.repository;

import com.example.issueservice.model.CommentModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<CommentModel, Long> {
    
    List<CommentModel> findByTicketIdOrderByCreatedAtAsc(Long ticketId);
    
    List<CommentModel> findByTicketIdAndIsInternalOrderByCreatedAtAsc(Long ticketId, Boolean isInternal);
}
