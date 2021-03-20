package com.bok.krypto;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableFeignClients
@EnableJpaRepositories("com.bok.krypto.repository")
public class KryptoApplication {

    public static void main(String[] args) {
        SpringApplication.run(KryptoApplication.class, args);
    }

}
