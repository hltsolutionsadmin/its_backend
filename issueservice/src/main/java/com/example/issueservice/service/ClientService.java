package com.example.issueservice.service;

import com.example.issueservice.dto.ClientDTO;
import com.example.issueservice.dto.CreateClientRequestDTO;
import com.example.issueservice.model.ClientModel;
import com.example.issueservice.repository.ClientRepository;
import com.its.commonservice.exception.ErrorCode;
import com.its.commonservice.exception.HltCustomerException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for client management
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;

    @Transactional
    public ClientDTO createClient(Long orgId, CreateClientRequestDTO request) {
        log.info("Creating client: {} in organization: {}", request.getEmail(), orgId);
        
        if (clientRepository.existsByOrganizationIdAndEmail(orgId, request.getEmail())) {
            throw new HltCustomerException(ErrorCode.CLIENT_EMAIL_EXISTS);
        }
        
        ClientModel client = new ClientModel();
        client.setOrganizationId(orgId);
        client.setName(request.getName());
        client.setEmail(request.getEmail());
        client.setPhone(request.getPhone());
        client.setCompany(request.getCompany());
        client.setActive(true);
        
        client = clientRepository.save(client);
        
        log.info("Client created successfully with ID: {}", client.getId());
        
        return buildClientDTO(client);
    }

    @Transactional(readOnly = true)
    public Page<ClientDTO> getOrganizationClients(Long orgId, Pageable pageable) {
        return clientRepository.findByOrganizationId(orgId, pageable)
            .map(this::buildClientDTO);
    }

    @Transactional(readOnly = true)
    public ClientDTO getClientById(Long clientId) {
        ClientModel client = clientRepository.findById(clientId)
            .orElseThrow(() -> new HltCustomerException(ErrorCode.CLIENT_NOT_FOUND));
        
        return buildClientDTO(client);
    }

    private ClientDTO buildClientDTO(ClientModel client) {
        return ClientDTO.builder()
            .id(client.getId())
            .organizationId(client.getOrganizationId())
            .name(client.getName())
            .email(client.getEmail())
            .phone(client.getPhone())
            .company(client.getCompany())
            .active(client.getActive())
            .createdAt(client.getCreatedAt())
            .build();
    }
}
