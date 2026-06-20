package com.feitu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FeituApplication {

    public static void main(String[] args) {
        SpringApplication.run(FeituApplication.class, args);
    }
}
