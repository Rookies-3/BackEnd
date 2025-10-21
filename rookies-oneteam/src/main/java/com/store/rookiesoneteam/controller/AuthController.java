package com.store.rookiesoneteam.controller;

import com.store.rookiesoneteam.dto.LoginDTO;
import com.store.rookiesoneteam.service.LoginService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final LoginService loginService;

    @Operation(summary = "로그인", description = "JWT 토큰과 함께 로그인할 사용자 정보를 DB에서 탐색하며 여러 데이터 검증을 거쳐 로그인 여부를 반환합니다.")
    @PostMapping("/login")
    public LoginDTO.Response login(@Valid @RequestBody LoginDTO.Request dto, HttpServletRequest request) {
        return loginService.login(dto, request);
    }
}
