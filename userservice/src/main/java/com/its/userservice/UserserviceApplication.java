package com.its.userservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
        "com.its.userservice",
        "com.its.commonservice"
})
public class UserserviceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserserviceApplication.class, args);
    }
}
