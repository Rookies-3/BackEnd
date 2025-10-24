package com.store.rookiesoneteam.service.impl;

import com.store.rookiesoneteam.dto.ChatMessageDto;
import com.store.rookiesoneteam.service.AIService;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class AIServiceImpl implements AIService {

    private final WebClient webClient;
    private final String aiServerEndpointBase;

    public AIServiceImpl(
            WebClient.Builder webClientBuilder,
            @Value("${ai.server.url}") String aiServerUrl,
            @Value("${ai.server.endpoint}") String aiServerEndpoint
    ) {
        this.webClient = webClientBuilder.baseUrl(aiServerUrl).build();
        this.aiServerEndpointBase = aiServerEndpoint;
    }

    @Getter
    @NoArgsConstructor
    private static class ChatResponse {
        private boolean success;
        private String reply;
        private String session_id;
        private Optional<List<Map<String, Object>>> contextSources = Optional.empty();
        private Optional<Map<String, Object>> evaluation = Optional.empty();
    }

    @Override
    @Async
    public CompletableFuture<String> getAIResponse(ChatMessageDto chatMessageDto) {
        //동적 URL 경로 생성
        String dynamicEndpoint = this.aiServerEndpointBase + "/" + chatMessageDto.getRoomId() + "/chat";

        log.info("AI 서버에 메시지 요청을 시작합니다: '{}', EndPoint: {}", chatMessageDto.getMessage(), dynamicEndpoint);

        Mono<ChatResponse> responseMono = webClient.post()
                .uri(dynamicEndpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(chatMessageDto) // ⭐️ AIRequest 대신 messageDto 전송
                .retrieve()
                .bodyToMono(ChatResponse.class);

        return responseMono
                .map(ChatResponse::getReply)
                // Null 방어 코드: mapper가 null을 반환하지 않도록 방지
                .filter(Objects::nonNull)
                .switchIfEmpty(Mono.error(new RuntimeException("AI response 'reply' field was null")))
                .onErrorResume(e -> {
                    log.error("AI 서버 통신 오류 발생: {}", e.getMessage(), e);

                    String errorMessage = """
                            AI 서버와의 통신에 실패했습니다. (Error: %s)
                            """.formatted(e.getMessage());
                    return Mono.just(errorMessage);
                })
                .toFuture();
    }
}