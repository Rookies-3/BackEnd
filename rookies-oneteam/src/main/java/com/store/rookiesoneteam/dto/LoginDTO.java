package com.store.rookiesoneteam.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class LoginDTO {
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        @NotBlank(message = "아이디(username)는 필수입니다.")
        @Size(min = 3, max = 50, message = "아이디는 3~50자 사이여야 합니다.")
        private String username;

        @NotBlank(message = "비밀번호는 필수입니다.")
        @Size(min = 8, max = 150, message = "비밀번호는 최소 8자 이상이어야 합니다.")
        private String password;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private String accessToken;
        private String refreshToken;
        private String username;
        private String role;
        private String message; // "로그인 성공/실패"

        private LocalDateTime lastLogin;
    }
}
