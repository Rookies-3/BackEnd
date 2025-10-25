package com.store.rookiesoneteam.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class EvaluateDto {

    /**
     * AI 서버(/evaluate)로 보낼 요청 DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        private Long roomId;
        private String transcript;
        private String evaluationType;
        private LocalDateTime createdAt;

        public Request(ChatMessageDto chatMessage) {
            this.roomId = chatMessage.getRoomId();
            this.transcript = chatMessage.getMessage();
            this.evaluationType = "interview"; // 기본값
            this.createdAt = chatMessage.getCreatedAt();
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private boolean success;
        private double score;
        private String feedback;
        private String session_id;
    }
}