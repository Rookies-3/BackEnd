package com.store.rookiesoneteam.dto;

import com.store.rookiesoneteam.domain.enums.UserRole;
import com.store.rookiesoneteam.domain.enums.UserStatus;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class UserDTO {
    // === 회원가입 === //
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        @NotBlank
        @Size(max = 50)
        private String username;

        @NotBlank
        @Size(max = 50)
        private String name;

        @NotBlank
        @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,150}$", message = "비밀번호는 8자 이상, 영문, 숫자, 특수문자를 포함해야 합니다.")
        private String password;

        @NotBlank
        @Pattern(regexp = "^\\d{10,11}$", message = "전화번호는 10~11자리 숫자여야 합니다.")
        private String phone;

        @NotBlank
        @Size(max = 50)
        private String nickname;

        @NotBlank
        @Email
        @Size(max = 100)
        private String email;
    }

    // === 내 정보 수정 (통합) === //
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MyInfoUpdateRequest {
        @NotBlank(message = "정보를 수정하려면 현재 비밀번호를 입력해야 합니다.")
        private String currentPassword;

        private String name;

        @Size(max = 50)
        private String nickname;

        @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,150}$", message = "새 비밀번호는 8자 이상, 영문, 숫자, 특수문자를 포함해야 합니다.")
        private String newPassword;

        @Email
        @Size(max = 100)
        private String email;

        @Pattern(regexp = "^\\d{10,11}$", message = "전화번호는 10~11자리 숫자여야 합니다.")
        private String phone;
    }

    // === 관리자용 유저 정보 수정 (참고용) === //
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateRequest {
        @NotNull
        private Long id;

        @NotBlank
        @Size(max = 50)
        private String username;

        @NotBlank
        @Size(max = 50)
        private String name;

        @NotBlank
        @Pattern(regexp = "^\\d{10,11}$", message = "전화번호는 10~11자리 숫자여야 합니다.")
        private String phone;

        @NotBlank
        @Size(max = 50)
        private String nickname;

        @NotBlank
        @Email
        @Size(max = 100)
        private String email;

        @NotNull
        private UserRole role;

        @NotNull
        private UserStatus status;
    }

    // === 사용자 응답 DTO === //
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private String username;
        private String name;
        private String phone;
        private String nickname;
        private String email;
        private UserRole role;
        private UserStatus status;
    }
}
