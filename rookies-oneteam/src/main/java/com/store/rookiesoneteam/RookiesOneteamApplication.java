package com.store.rookiesoneteam;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync; // ⭐️ [추가]

@EnableAsync // ⭐️ [추가] 비동기 처리를 활성화합니다.
@SpringBootApplication
public class RookiesOneteamApplication {
    public static void main(String[] args) {
        SpringApplication.run(RookiesOneteamApplication.class, args);
    }
}