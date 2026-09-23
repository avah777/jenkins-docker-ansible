package com.example.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class Application {

    @GetMapping("/")
    public String home() {
        return "Hello from Jenkins + Docker + Ansible + AWS!";
    }

    @GetMapping("/health")
    public String health() {
        return "Application is healthy";
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
