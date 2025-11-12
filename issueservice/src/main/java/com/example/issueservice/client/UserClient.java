package com.example.issueservice.client;

import com.its.common.dto.UserDTO;
import com.its.commonservice.dto.StandardResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "user-service-users",
        url = "${services.user.base-url:http://localhost:8082}",
        path = "/api/users"
)
public interface UserClient {

    @GetMapping("/{userId}")
    StandardResponse<UserDTO> getUserById(@PathVariable("userId") Long userId);

    @PostMapping("/save")
    StandardResponse<UserDTO> saveUser(@RequestBody UserDTO user);

    @GetMapping("/{email}/email")
    StandardResponse<UserDTO> getUserByEmail(@PathVariable("email") String email);

}
