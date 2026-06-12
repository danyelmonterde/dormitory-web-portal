package com.example.dormportal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DormPortalApplication {
    public static void main(String[] args) {
        SpringApplication.run(DormPortalApplication.class, args);
    }
}
