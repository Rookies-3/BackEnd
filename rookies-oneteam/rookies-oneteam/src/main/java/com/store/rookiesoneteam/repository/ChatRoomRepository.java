package com.store.rookiesoneteam.repository;

import com.store.rookiesoneteam.domain.entity.ChatRoom;
import com.store.rookiesoneteam.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    // 사용자로 모든 채팅방을 찾아 생성 시간 역순으로 정렬
    List<ChatRoom> findByUserOrderByCreatedAtDesc(User user);
}
