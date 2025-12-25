package com.korconstructions;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
        System.out.println("\n========================================");
        System.out.println("Application started successfully!");
        System.out.println("Open your browser at: http://localhost:8080");
        System.out.println("========================================\n");
    }
}
