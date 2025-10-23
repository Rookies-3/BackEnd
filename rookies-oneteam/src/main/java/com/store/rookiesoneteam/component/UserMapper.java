package com.store.rookiesoneteam.component;

import com.store.rookiesoneteam.domain.entity.User;
import com.store.rookiesoneteam.domain.enums.UserRole;
import com.store.rookiesoneteam.domain.enums.UserStatus;
import com.store.rookiesoneteam.dto.UserDTO;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    // 엔티티 → DTO 변환 (관리자용)
    public static UserDTO.UpdateRequest toUpdateRequest(User user) {
        return UserDTO.UpdateRequest.builder()
                .id(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .phone(user.getPhone())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .role(user.getRole())
                .status(user.getStatus())
                .build();
    }

    // 회원가입 DTO -> 엔티티 변환
    public User toEntity(UserDTO.Request dto, PasswordEncoder passwordEncoder) {
        return User.builder()
                .username(dto.getUsername())
                .name(dto.getName())
                .password(passwordEncoder.encode(dto.getPassword()))
                .phone(dto.getPhone())
                .nickname(dto.getNickname())
                .email(dto.getEmail())
                .role(UserRole.USER) // 기본 역할은 USER
                .status(UserStatus.ACTIVE) // 가입 시 기본 상태는 ACTIVE
                .build();
    }

    // 엔티티 -> 응답 DTO 변환
    public UserDTO.Response toResponse(User user) {
        return UserDTO.Response.builder()
                .id(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .phone(user.getPhone())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .role(user.getRole())
                .status(user.getStatus())
                .build();
    }
}
