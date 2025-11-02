package com.example.issueservice.repository;

import com.example.issueservice.model.TicketHistoryModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketHistoryRepository extends JpaRepository<TicketHistoryModel, Long> {
    
    List<TicketHistoryModel> findByTicketIdOrderByCreatedAtDesc(Long ticketId);
}
