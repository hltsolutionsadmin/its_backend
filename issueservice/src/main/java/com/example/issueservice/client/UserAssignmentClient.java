package com.example.issueservice.client;

import com.its.common.dto.UserAssignmentDTO;
import com.its.commonservice.dto.StandardResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "user-service-assignments",
        url = "${services.user.base-url:https://localhost:9443}",
        path = "/api/assignments"
)
public interface UserAssignmentClient {

    @GetMapping("/user/{userId}")
    StandardResponse<UserAssignmentDTO> getAssignmentsByUser(
            @PathVariable("userId") Long userId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "50") int size,
            @RequestParam(name = "sortBy", defaultValue = "id") String sortBy,
            @RequestParam(name = "direction", defaultValue = "ASC") String direction
    );
}
