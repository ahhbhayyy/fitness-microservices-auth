package com.fitness.aiservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@EnableKafka
@SpringBootApplication
public class AiserviceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiserviceApplication.class, args);
    }

}
