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
        return "Version 2 deployed automatically by Jenkins using webhook!";
    }

    @GetMapping("/health")
    public String health() {
        return "Application is healthy";
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
