package com.example.issueservice.client;

import com.its.common.dto.UserGroupDTO;
import com.its.commonservice.dto.StandardResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "user-service",
        url = "${services.user.base-url:http://localhost:8082}",
        path = "/api/usergroups"
)
public interface UserServiceClient {

    @GetMapping("/{id}")
    StandardResponse<UserGroupDTO> getUserGroupById(@PathVariable("id") Long id);
}
