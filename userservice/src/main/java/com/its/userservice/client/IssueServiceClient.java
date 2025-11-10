package com.its.userservice.client;

import com.its.common.dto.ProjectDTO;
import com.its.commonservice.dto.StandardResponse;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "issue-service",
        url = "${services.issue.base-url:https://localhost:9443/api/usermanagement}",
        configuration = FeignNoAuthConfig.class,
        path = "/projects"
)
public interface IssueServiceClient {

    @GetMapping("/{projectId}")
    StandardResponse<ProjectDTO> getProjectById(@PathVariable("projectId") Long projectId);
}
