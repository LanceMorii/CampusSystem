package com.example.campussystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
@EnableAspectJAutoProxy
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class CampusSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusSystemApplication.class, args);
    }

}