package com.store.rookiesoneteam.dto;

import com.store.rookiesoneteam.domain.entity.ChatRoom;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class ChatRoomDto {

    // 채팅방 생성을 위한 요청 DTO
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        private String roomName;
    }

    // 클라이언트에게 전달할 응답 DTO
    @Getter
    @Builder
    public static class Response {
        private Long roomId;
        private String roomName;
        private LocalDateTime createdAt;

        // Entity를 DTO로 변환하는 정적 메소드
        public static Response fromEntity(ChatRoom chatRoom) {
            return Response.builder()
                    .roomId(chatRoom.getId())
                    .roomName(chatRoom.getRoomName())
                    .createdAt(chatRoom.getCreatedAt())
                    .build();
        }
    }
}
