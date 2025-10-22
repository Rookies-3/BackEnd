// rookies-oneteam/src/main/java/com/store/rookiesoneteam/service/impl/LoginServiceImpl.java (수정)
package com.store.rookiesoneteam.service.impl;

import com.store.rookiesoneteam.component.JwtTokenProvider;
import com.store.rookiesoneteam.domain.entity.LoginAttemptHistory; // [추가]
import com.store.rookiesoneteam.domain.entity.User;
import com.store.rookiesoneteam.dto.LoginDTO;
import com.store.rookiesoneteam.repository.LoginAttemptHistoryRepository; // [추가]
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
    private final LoginAttemptHistoryRepository loginAttemptHistoryRepository; // [추가]

    @Override
    @Transactional
    public LoginDTO.Response login(LoginDTO.Request dto, HttpServletRequest request) {
        // 유저 존재 여부 확인
        User user = userRepository.findByUsername(dto.getUsername())
                .orElse(null);

        // 유저 상태에 따른 history 삽입
        // 존재하지 않는 경우
        if(user == null) {
            recordAttempt(dto.getUsername(), false, "login failed - 존재하지 않는 계정", request); // [활성화]
            throw new BadCredentialsException("존재하지 않는 사용자입니다.");
        }

        // 비밀번호 검증 시 미일치의 경우
        if(user != null) {
            // status 체크
            switch(user.getStatus()) {
                case INACTIVE -> {
                    recordAttempt(user.getUsername(), false, "휴면 계정 로그인 시도", request); // [활성화]
                    throw new IllegalStateException("휴면 계정입니다. 고객센터에 문의하세요.");
                }
                case SUSPENDED -> {
                    recordAttempt(user.getUsername(), false,"정지된 계정 로그인 시도", request); // [활성화]
                    throw new IllegalStateException("정지된 계정입니다.");
                }
                case DELETED -> {
                    recordAttempt(user.getUsername(), false, "탈퇴 계정 로그인 시도", request); // [활성화]
                    throw new IllegalStateException("탈퇴 처리된 계정입니다.");
                }
                case PENDING -> {
                    recordAttempt(user.getUsername(), false, "이메일 인증 미완료 로그인 시도", request); // [활성화]
                    throw new IllegalStateException("이메일 인증 후 로그인 가능합니다.");
                }
                case BLOCKED -> {
                    recordAttempt(user.getUsername(), false, "관리자 차단 계정 로그인 시도", request); // [활성화]
                    throw new IllegalStateException("관리자에 의해 차단된 계정입니다.");
                }
                case ACTIVE -> {
                    // 정상 로그인 시 비밀번호 일치 여부 체크
                    if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
                        recordAttempt(user.getUsername(), false, "login failed - 비밀번호 불일치", request); // [활성화]
                        checkLoginAttempt(user); // 실패 횟수 체크 // [활성화]
                        throw new BadCredentialsException("비밀번호가 일치하지 않습니다.");
                    }

                    String reason = "";
                    switch (user.getRole()) {
                        case ADMIN -> reason = "login - 관리자";
                        case USER1 -> reason = "login - 사용자 첫번째 유형";
                        case USER2 -> reason = "login - 사용자 두번째 유형";
                    }

                    recordAttempt(user.getUsername(), true, reason, request); // [활성화]

                    // JWT 발급
                    Map<String, Object> claims = Map.of("role", user.getRole().name());
                    String token = jwtTokenProvider.createToken(user.getEmail(), user.getRole().name(), user.getNickname(), user.getUsername(),  claims);

                    //TODO refresh Token 적용해보기

                    log.info("로그인 성공: email={}", user.getEmail());

                    // 마지막 로그인 시간 업데이트
                    user.updateLastLoginTime();

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

    // [활성화 및 수정] 로그인 시도 기록 메서드
    private void recordAttempt(String username, boolean success, String reason, HttpServletRequest request) {
        // IP 주소를 정확히 가져오는 로직은 복잡하므로 간단히 getRemoteAddr() 사용
        String ipAddress = request.getRemoteAddr();

        LoginAttemptHistory history = LoginAttemptHistory.builder()
                .username(username)
                .success(success)
                .reason(reason)
                .ipAddress(ipAddress)
                .attemptedAt(LocalDateTime.now())
                .build();
        loginAttemptHistoryRepository.save(history);
    }

    // [활성화 및 수정] 로그인 실패 횟수 체크 및 계정 차단 메서드
    @Transactional // User 엔티티의 상태 변경을 위해 필요
    private void checkLoginAttempt(User user) {
        // 최근 5개 시도 기록을 내림차순으로 가져옴
        List<LoginAttemptHistory> attempts = loginAttemptHistoryRepository.findTop5ByUsernameOrderByAttemptedAtDesc(user.getUsername());

        // 연속 실패 횟수 계산
        long failsCnt = attempts.stream()
                .filter(a -> !a.isSuccess())
                .count();

        // 연속 실패 횟수가 MAX_LOGIN_ATTEMPTS를 초과하면 계정 차단
        if(failsCnt >= MAX_LOGIN_ATTEMPTS) {
            user.blocked();
            userRepository.save(user);
            throw new IllegalStateException("로그인 실패 횟수 초과로 계정이 차단됐습니다. 관리자에게 문의하세요.");
        }
    }
}