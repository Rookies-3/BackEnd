package com.store.rookiesoneteam.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.store.rookiesoneteam.domain.enums.MessageType;
import com.store.rookiesoneteam.dto.ChatDto;
import com.store.rookiesoneteam.dto.ChatMessageDto;
import com.store.rookiesoneteam.dto.EvaluateDto;
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

import java.time.LocalDateTime;
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
    private final ObjectMapper objectMapper;

    public AIServiceImpl(
            WebClient.Builder webClientBuilder,
            @Value("${ai.server.url}") String aiServerUrl,
            @Value("${ai.server.endpoint}") String aiServerEndpoint,
            ObjectMapper objectMapper
    ) {
        this.webClient = webClientBuilder.baseUrl(aiServerUrl).build();
        this.aiServerEndpointBase = aiServerEndpoint;
        this.objectMapper = objectMapper;
    }

    @Override
    @Async
    public CompletableFuture<String> getAIResponse(ChatMessageDto messageDto) {
        if (messageDto.getType() == MessageType.TALK) {
            return getChatResponse(messageDto);
        } else if (messageDto.getType() == MessageType.EVALUATE) {
            return getEvaluateResponse(messageDto);
        } else {
            log.warn("지원하지 않는 메시지 타입입니다: {}", messageDto.getType());
            return CompletableFuture.completedFuture("지원하지 않는 메시지 타입입니다.");
        }
    }

    private CompletableFuture<String> getChatResponse(ChatMessageDto messageDto) {
        String dynamicEndpoint = this.aiServerEndpointBase + "/" + messageDto.getRoomId() + "/chat";
        log.info("AI 채팅 요청: '{}', EndPoint: {}", messageDto.getMessage(), dynamicEndpoint);

        return webClient.post()
                .uri(dynamicEndpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(messageDto)
                .retrieve()
                .bodyToMono(ChatDto.Response.class)
                .map(ChatDto.Response::getReply)
                .filter(Objects::nonNull)
                .switchIfEmpty(Mono.error(new RuntimeException("AI chat response 'reply' field was null")))
                .onErrorResume(e -> {
                    log.error("AI 채팅 서버 통신 오류: {}", e.getMessage(), e);
                    return Mono.just("AI 채팅 서버와의 통신에 실패했습니다: " + e.getMessage());
                })
                .toFuture();
    }

    private CompletableFuture<String> getEvaluateResponse(ChatMessageDto messageDto) {
        String dynamicEndpoint = this.aiServerEndpointBase + "/" + messageDto.getRoomId() + "/evaluate";
        log.info("AI 평가 요청: '{}', EndPoint: {}", messageDto.getMessage(), dynamicEndpoint);

        EvaluateDto.Request evaluateRequest = new EvaluateDto.Request(messageDto);

        return webClient.post()
                .uri(dynamicEndpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(evaluateRequest)
                .retrieve()
                .bodyToMono(EvaluateDto.Response.class)
                .filter(Objects::nonNull)
                .switchIfEmpty(Mono.error(new RuntimeException("AI evaluation response 'feedback' field was null")))
                .<String>handle((evaluateResponse, sink) -> {
                    try {
                        sink.next(objectMapper.writeValueAsString(evaluateResponse));
                    } catch (JsonProcessingException e) {
                        log.error("EvaluateDto.Response 직렬화 실패", e);
                        sink.error(new RuntimeException("Response DTO 직렬화 실패", e));
                    }
                })
                .onErrorResume(e -> {
                    log.error("AI 평가 서버 통신 오류: {}", e.getMessage(), e);
                    String errorJson = String.format(
                            "{\"success\":false, \"score\":0.0, \"feedback\":\"AI 평가 서버와의 통신에 실패했습니다: %s\"}",
                            e.getMessage().replace("\"", "'") // 간단한 JSON 이스케이프
                    );

                    return Mono.just("AI 평가 서버와의 통신에 실패했습니다: " + e.getMessage());
                })
                .toFuture();
    }
}