package com.store.rookiesoneteam.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class) // 생성 시간을 자동으로 기록하기 위해 추가
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 메시지는 반드시 채팅방에 속해야 함 (N:1 관계)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    // 메시지 보낸 사람 (사용자 닉네임 또는 "AI")
    @Column(nullable = false)
    private String sender;

    @Lob
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String message;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public ChatMessage(ChatRoom chatRoom, String sender, String message) {
        this.chatRoom = chatRoom;
        this.sender = sender;
        this.message = message;
    }
}
