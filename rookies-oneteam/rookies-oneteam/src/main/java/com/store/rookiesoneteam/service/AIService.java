package com.store.rookiesoneteam.service;

import com.store.rookiesoneteam.dto.ChatMessageDto;

import java.util.concurrent.CompletableFuture;

public interface AIService {

    /**
     * 주어진 메시지에 대한 AI의 응답을 비동기적으로 반환합니다.
     * @Param ChatMessageDto 사용자의 메시지 정보
     * @return AI의 응답을 담은 CompletableFuture
     */
    CompletableFuture<String> getAIResponse(ChatMessageDto chatMessageDto);
}
