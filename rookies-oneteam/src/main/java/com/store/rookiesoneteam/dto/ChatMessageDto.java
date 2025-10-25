package com.store.rookiesoneteam.dto;

import com.store.rookiesoneteam.domain.enums.MessageType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDto {
    private MessageType type;
    private Long roomId;      // 방 번호
    private String sender;    // 보낸 사람
    private String message;   // 메시지 내용
    private LocalDateTime createdAt; // 보낸 시간 (클라이언트에 전송 시 사용)
}
