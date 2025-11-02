package com.example.issueservice.controller;

import com.example.issueservice.dto.ClientDTO;
import com.example.issueservice.dto.CreateClientRequestDTO;
import com.example.issueservice.service.ClientService;
import com.juvarya.commonservice.dto.StandardResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for client management endpoints
 */
@RestController
@RequestMapping("/api/orgs/{orgId}/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    @PostMapping
    public StandardResponse<ClientDTO> createClient(
            @PathVariable Long orgId,
            @Valid @RequestBody CreateClientRequestDTO request) {
        
        ClientDTO client = clientService.createClient(orgId, request);
        return StandardResponse.single(client, "Client created successfully");
    }

    @GetMapping
    public StandardResponse<ClientDTO> getClients(
            @PathVariable Long orgId,
            Pageable pageable) {
        
        Page<ClientDTO> clients = clientService.getOrganizationClients(orgId, pageable);
        return StandardResponse.page(clients);
    }

    @GetMapping("/{clientId}")
    public StandardResponse<ClientDTO> getClient(@PathVariable Long clientId) {
        ClientDTO client = clientService.getClientById(clientId);
        return StandardResponse.single(client);
    }
}
