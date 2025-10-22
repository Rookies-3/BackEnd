package com.store.rookiesoneteam.service;

import com.store.rookiesoneteam.dto.LoginDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;

public interface LoginService {
    LoginDTO.Response login(LoginDTO.Request dto, HttpServletRequest request);

    /**
     * JWT 토큰에서 추출한 이메일을 기반으로 Authentication 객체를 생성하여 반환합니다.
     * 이 객체는 웹소켓 연결 시 사용자의 신원을 증명하는 데 사용됩니다.
     * @param email JWT 토큰에서 추출한 사용자 이메일
     * @return Spring Security가 사용하는 인증 객체
     */
    Authentication getAuthentication(String email);
}
