package com.store.rookiesoneteam.service.impl;

import com.store.rookiesoneteam.component.JwtTokenProvider;
import com.store.rookiesoneteam.domain.entity.User;
import com.store.rookiesoneteam.dto.LoginDTO;
import com.store.rookiesoneteam.repository.UserRepository;
import com.store.rookiesoneteam.service.LoginService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service("loginServiceImpl")
@RequiredArgsConstructor
@Slf4j
public class LoginServiceImpl implements LoginService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional
    public LoginDTO.Response login(LoginDTO.Request dto, HttpServletRequest request) {
        // 사용자 조회
        User user = userRepository.findByUsername(dto.getUsername())
                .orElseThrow(() -> new BadCredentialsException("존재하지 않는 사용자입니다."));

        // 계정 상태 확인
        switch (user.getStatus()) {
            case INACTIVE, SUSPENDED, DELETED, PENDING, BLOCKED ->
                    throw new IllegalStateException("로그인할 수 없는 계정 상태입니다: " + user.getStatus());
            case ACTIVE -> {
                // 비밀번호 확인
                if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
                    throw new BadCredentialsException("비밀번호가 일치하지 않습니다.");
                }

                // JWT 토큰 생성
                Map<String, Object> claims = Map.of("role", user.getRole().name());
                String token = jwtTokenProvider.createToken(user.getEmail(), user.getRole().name(), user.getNickname(), user.getUsername(), claims);

                log.info("로그인 성공: email={}", user.getEmail());

                // 응답 생성
                return LoginDTO.Response.builder()
                        .username(user.getUsername())
                        .role(user.getRole().name())
                        .accessToken(token)
                        .message("로그인 성공")
                        .lastLogin(LocalDateTime.now())
                        .build();
            }
            default -> throw new IllegalStateException("정의되지 않은 사용자 상태입니다.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Authentication getAuthentication(String email) {
        // 이메일로 사용자 정보 조회
        UserDetails userDetails = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // Spring Security가 사용하는 Authentication 객체 생성 후 반환
        // 웹소켓 연결 시에는 비밀번호가 필요 없으므로 비워둠
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }
}
