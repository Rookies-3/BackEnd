package com.store.rookiesoneteam.service.impl;

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

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class AIServiceImpl implements AIService {

    private final WebClient webClient;
    private final String aiServerEndpoint; // ⭐️ [추가] 엔드포인트 필드

    // application.properties에서 AI 서버 URL과 엔드포인트를 주입받아 WebClient를 초기화합니다.
    public AIServiceImpl(
            WebClient.Builder webClientBuilder,
            @Value("${ai.server.url}") String aiServerUrl,
            @Value("${ai.server.endpoint}") String aiServerEndpoint // ⭐️ [변경] 엔드포인트 주입
    ) {
        this.webClient = webClientBuilder.baseUrl(aiServerUrl).build();
        this.aiServerEndpoint = aiServerEndpoint; // ⭐️ [추가] 엔드포인트 필드 초기화
    }

    // ⭐️ [DTO 변경] AI 서버로 보낼 요청 DTO (RAGQueryRequest에 맞게 수정)
    @Getter
    @NoArgsConstructor
    private static class AIRequest {
        private String question; // ⭐️ [변경] user_message -> question
        private boolean use_external_knowledge = false; // ⭐️ [추가] FastAPI 모델에 맞춤
        private int k = 4; // ⭐️ [추가] FastAPI 모델에 맞춤

        public AIRequest(String userMessage) {
            this.question = userMessage;
        }
    }

    // ⭐️ [DTO 변경] AI 서버로부터 받을 응답 DTO (RAGQueryResponse에 맞게 수정)
    @Getter
    @NoArgsConstructor
    private static class AIResponse {
        private String answer; // ⭐️ [변경] ai_response -> answer
    }

    @Override
    @Async // 이 메소드를 비동기 스레드에서 실행하도록 설정합니다.
    public CompletableFuture<String> getAIResponse(String userMessage) {
        // ⭐️ [변경] application.properties에서 주입받은 엔드포인트를 사용합니다.
        String endpoint = this.aiServerEndpoint;
        log.info("AI 서버에 메시지 요청을 시작합니다: '{}', EndPoint: {}", userMessage, endpoint);

        // ⭐️ [변경] RAGQueryRequest 모델에 맞게 객체 생성
        AIRequest request = new AIRequest(userMessage);

        // WebClient를 사용하여 비동기 POST 요청을 보냅니다.
        Mono<AIResponse> responseMono = webClient.post()
                .uri(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request) // JSON 요청 본문 설정
                .retrieve()
                // HTTP 상태 코드 4xx, 5xx 에러는 기본적으로 throw됨
                .bodyToMono(AIResponse.class); // 응답을 AIResponse 객체로 받음

        // Mono(Reactive Type)를 CompletableFuture로 변환하고, 응답 객체에서 메시지만 추출합니다.
        return responseMono
                // ⭐️ [변경] AIResponse 객체에서 answer 필드만 추출
                .map(AIResponse::getAnswer)
                .onErrorResume(e -> {
                    log.error("AI 서버 통신 오류 발생: {}", e.getMessage(), e);
                    // 통신 또는 처리 오류 발생 시 대체 응답을 반환하여 CompletableFuture를 완료합니다.
                    return Mono.just("AI 서버와의 통신에 실패했습니다. (Error: " + e.getMessage() + ")");
                })
                .toFuture(); // CompletableFuture<String>으로 최종 변환
    }
}