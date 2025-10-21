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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service("loginServiceImpl")
@RequiredArgsConstructor
@Slf4j
public class LoginServiceImpl implements LoginService {
    private static final int MAX_LOGIN_ATTEMPTS = 5; // 최대 로그인 시도 횟수
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional
    public LoginDTO.Response login(LoginDTO.Request dto, HttpServletRequest request) {
        // 유저 존재 여부 확인
        User user = userRepository.findByUsername(dto.getUsername())
                .orElse(null);

        // 유저 상태에 따른 history 삽입
        // 존재하지 않는 경우
        if(user == null) {
//            recordAttempt(dto.getUsername(), false, "login failed - 존재하지 않는 계정", request);
            throw new BadCredentialsException("존재하지 않는 사용자입니다.");
        }

        // 비밀번호 검증 시 미일치의 경우
        if(user != null) {
            // status 체크
            switch(user.getStatus()) {
                case INACTIVE -> {
//                    recordAttempt(user.getUsername(), false, "휴면 계정 로그인 시도", request);
                    throw new IllegalStateException("휴면 계정입니다. 고객센터에 문의하세요.");
                }
                case SUSPENDED -> {
//                    recordAttempt(user.getUsername(), false,"정지된 계정 로그인 시도", request);
                    throw new IllegalStateException("정지된 계정입니다.");
                }
                case DELETED -> {
//                    recordAttempt(user.getUsername(), false, "탈퇴 계정 로그인 시도", request);
                    throw new IllegalStateException("탈퇴 처리된 계정입니다.");
                }
                case PENDING -> {
//                    recordAttempt(user.getUsername(), false, "이메일 인증 미완료 로그인 시도", request);
                    throw new IllegalStateException("이메일 인증 후 로그인 가능합니다.");
                }
                case BLOCKED -> {
//                    recordAttempt(user.getUsername(), false, "관리자 차단 계정 로그인 시도", request);
                    throw new IllegalStateException("관리자에 의해 차단된 계정입니다.");
                }
                case ACTIVE -> {
                    // 정상 로그인 시 비밀번호 일치 여부 체크
                    if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
//                        recordAttempt(user.getUsername(), false, "login failed - 비밀번호 불일치", request);
//                        checkLoginAttempt(user); // 실패 횟수 체크
                        throw new BadCredentialsException("비밀번호가 일치하지 않습니다.");
                    }

                    String reason = "";
                    switch (user.getRole()) {
                        case ADMIN -> reason = "login - 관리자";
                        case USER -> reason = "login - 사용자 유형";
                    }

//                    recordAttempt(user.getUsername(), true, reason, request);

                    // JWT 발급
                    Map<String, Object> claims = Map.of("role", user.getRole().name());
                    String token = jwtTokenProvider.createToken(user.getEmail(), user.getRole().name(), user.getNickname(), user.getUsername(),  claims);

                    //TODO refresh Token 적용해보기

                    log.info("로그인 성공: email={}", user.getEmail());

                    return LoginDTO.Response.builder()
                            .username(user.getUsername())
                            .role(user.getRole().name())
                            .accessToken(token)
                            .message("로그인 성공")
                            .lastLogin(LocalDateTime.now())
                            .build();
                }
            }
            throw new IllegalStateException("정의되지 않은 상태입니다.");
        }
        return null;
    }

//    private void recordAttempt(String username, boolean success, String reason, HttpServletRequest request) {
//        LoginAttemptHistory history = LoginAttemptHistory.builder()
//                .username(username)
//                .success(success)
//                .reason(reason)
//                .ipAddress(request.getRemoteAddr())
//                .attemptedAt(LocalDateTime.now())
//                .build();
//        loginAttemptHistoryRepository.save(history);
//    }

//    private void checkLoginAttempt(User user) {
//        List<LoginAttemptHistory> attempts = loginAttemptHistoryRepository.findTop5ByUsernameOrderByAttemptedAtDesc(user.getUsername());
//
//        long failsCnt = attempts.stream().takeWhile(a -> !a.isSuccess()).count();
//
//        if(failsCnt >= MAX_LOGIN_ATTEMPTS) {
//            user.blocked();
//            userRepository.save(user);
//            throw new IllegalStateException("로그인 실패 횟수 추가로 계정이 차단됐습니다.");
//        }
//    }
}

