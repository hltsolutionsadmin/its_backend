package com.example.issueservice.repository;

import com.example.issueservice.model.AttachmentModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttachmentRepository extends JpaRepository<AttachmentModel, Long> {
    
    List<AttachmentModel> findByTicketId(Long ticketId);
}
