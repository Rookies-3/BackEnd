package com.store.rookiesoneteam.component;

import com.store.rookiesoneteam.service.LoginService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StompHandler implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;
    private final LoginService loginService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        // STOMP가 연결(CONNECT)을 시도할 때만 토큰 검증을 수행합니다.
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            // 헤더에서 'Authorization' 값을 찾아 JWT 토큰을 추출합니다.
            String jwtToken = accessor.getFirstNativeHeader("Authorization");

            // 'Bearer ' 접두사를 제거하여 순수한 토큰만 추출합니다.
            if (jwtToken != null && jwtToken.startsWith("Bearer ")) {
                jwtToken = jwtToken.substring(7);
            }

            log.info("Attempting to authenticate STOMP connection with token: {}", jwtToken);

            // 토큰 유효성 검사를 수행합니다.
            if (jwtToken != null && jwtTokenProvider.validateToken(jwtToken)) {
                // 토큰이 유효하면, 토큰에서 이메일을 추출하여 인증 객체를 생성합니다.
                String email = jwtTokenProvider.getEmailFromToken(jwtToken);
                Authentication authentication = loginService.getAuthentication(email);
                // Spring Security 컨텍스트에 인증 정보를 저장하여, 이후 요청에서 사용자를 식별할 수 있도록 합니다.
                SecurityContextHolder.getContext().setAuthentication(authentication);
                accessor.setUser(authentication);
                log.info("STOMP connection authenticated successfully for user: {}", email);
            } else {
                log.warn("STOMP connection failed: Invalid or missing JWT token.");
                throw new AccessDeniedException("Access denied. Invalid JWT token.");
            }
        }
        return message;
    }
}
