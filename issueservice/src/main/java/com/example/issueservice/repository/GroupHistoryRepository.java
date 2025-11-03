package com.example.issueservice.repository;

import com.example.issueservice.model.GroupHistoryModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupHistoryRepository extends JpaRepository<GroupHistoryModel, Long> {
    List<GroupHistoryModel> findByTicketIdOrderByChangedAtAsc(Long ticketId);
}
