package com.example.issueservice.repository;

import com.example.issueservice.model.TicketCommentModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketCommentRepository extends JpaRepository<TicketCommentModel, Long> {

    List<TicketCommentModel> findByTicketIdOrderByCreatedAtAsc(Long ticketId);

}
