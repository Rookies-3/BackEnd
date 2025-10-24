package com.store.rookiesoneteam.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter; // ⭐️ Setter 임포트 추가
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter // ⭐️ 추가: roomName을 업데이트하기 위해 Setter 어노테이션 추가
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String roomName;

    // cascade = CascadeType.ALL: 부모(ChatRoom)가 삭제될 때, 자식(ChatMessage)도 모두 삭제됩니다.
    // orphanRemoval = true: 부모와 연결이 끊어진 자식은 자동으로 삭제됩니다.
    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatMessage> messages = new ArrayList<>();

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public ChatRoom(User user, String roomName) {
        this.user = user;
        this.roomName = roomName;
    }
}