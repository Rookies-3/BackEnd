package com.store.rookiesoneteam.config;

import com.store.rookiesoneteam.component.StompHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final StompHandler stompHandler; // JWT 인증을 위한 핸들러

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 클라이언트가 메시지를 구독할 때 사용하는 경로(prefix)입니다.
        // 예: /sub/chat/room/1
        registry.enableSimpleBroker("/sub");

        // 클라이언트가 서버로 메시지를 보낼 때 사용하는 경로(prefix)입니다.
        // 예: /pub/chat/message
        registry.setApplicationDestinationPrefixes("/pub");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 클라이언트가 최초로 웹소켓에 연결할 때 사용할 주소(endpoint)입니다.
        registry.addEndpoint("/ws-stomp")
                .setAllowedOriginPatterns("*") // 모든 외부 도메인에서의 접속을 허용합니다. (개발 초기 설정)
                .withSockJS(); // 웹소켓을 지원하지 않는 브라우저를 위한 대체 옵션
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // 클라이언트가 서버로 메시지를 보내기 전에, 우리가 만든 StompHandler를 거치도록 설정합니다.
        // 이 핸들러가 JWT 토큰을 검사하는 역할을 합니다.
        registration.interceptors(stompHandler);
    }
}
