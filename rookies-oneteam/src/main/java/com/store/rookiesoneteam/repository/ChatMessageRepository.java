package com.store.rookiesoneteam.repository;

import com.store.rookiesoneteam.domain.entity.ChatMessage;
import com.store.rookiesoneteam.domain.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // 특정 채팅방의 모든 메시지를 생성 시간 오름차순으로 조회
    List<ChatMessage> findAllByChatRoomOrderByCreatedAtAsc(ChatRoom chatRoom);
}
