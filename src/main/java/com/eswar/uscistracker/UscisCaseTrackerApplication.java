package com.eswar.uscistracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class UscisCaseTrackerApplication {
    public static void main(String[] args) {
        SpringApplication.run(UscisCaseTrackerApplication.class, args);
    }
}
