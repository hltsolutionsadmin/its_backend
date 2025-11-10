package com.example.issueservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableScheduling
@EnableFeignClients(basePackages = {"com.example.issueservice.client"})
public class IssueServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(IssueServiceApplication.class, args);
    }
}
