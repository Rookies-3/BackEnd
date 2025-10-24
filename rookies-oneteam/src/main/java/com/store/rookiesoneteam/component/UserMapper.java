package com.store.rookiesoneteam.component;

import com.store.rookiesoneteam.domain.entity.User;
import com.store.rookiesoneteam.domain.enums.UserRole;
import com.store.rookiesoneteam.domain.enums.UserStatus;
import com.store.rookiesoneteam.dto.UserDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapper {

    private final PasswordEncoder passwordEncoder;

    // 엔티티 → 관리자용 응답 DTO 변환
    public UserDTO.AdminResponse toAdminResponse(User user) {
        return UserDTO.AdminResponse.builder()
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
    public User toEntity(UserDTO.Request dto) {
        return User.builder()
                .username(dto.getUsername())
                .name(dto.getName())
                .password(passwordEncoder.encode(dto.getPassword()))
                .phone(dto.getPhone())
                .nickname(dto.getNickname())
                .email(dto.getEmail())
                .role(UserRole.USER) // 역할은 서버에서 직접 지정
                .status(UserStatus.ACTIVE) // 상태는 서버에서 직접 지정
                .build();
    }

    // 엔티티 -> 일반 사용자 응답 DTO 변환
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
