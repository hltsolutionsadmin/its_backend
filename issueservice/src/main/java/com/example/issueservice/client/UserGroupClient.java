package com.example.issueservice.client;

import com.its.common.dto.UserGroupDTO;
import com.its.commonservice.dto.StandardResponse;
import com.its.commonservice.enums.TicketPriority;
import com.its.commonservice.enums.TicketStatus;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "user-service-groups",
        url = "${services.user.base-url:http://localhost:8082}",
        path = "/api/usergroups"
)
public interface UserGroupClient {

    @GetMapping("/{projectId}/{priority}")
    StandardResponse<UserGroupDTO> getGroupsByProjectAndPriority(
            @PathVariable("projectId") Long projectId,
            @PathVariable("priority") TicketPriority priority
    );

    @GetMapping("/{id}")
    StandardResponse<UserGroupDTO> getById(@PathVariable("id") Long id);
}
