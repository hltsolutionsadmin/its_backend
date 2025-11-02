package com.example.issueservice.repository;

import com.example.issueservice.model.ClientModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<ClientModel, Long> {
    
    Page<ClientModel> findByOrganizationId(Long organizationId, Pageable pageable);
    
    Optional<ClientModel> findByOrganizationIdAndEmail(Long organizationId, String email);
    
    boolean existsByOrganizationIdAndEmail(Long organizationId, String email);
}
