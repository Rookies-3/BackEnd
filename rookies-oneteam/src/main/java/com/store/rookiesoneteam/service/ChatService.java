package com.store.rookiesoneteam.service;

import com.store.rookiesoneteam.dto.ChatMessageDto;
import com.store.rookiesoneteam.dto.ChatRoomDto;

import java.util.List;

public interface ChatService {

    /**
     * 사용자가 보낸 메시지를 처리하고, AI의 응답을 요청합니다.
     */
    void processMessage(ChatMessageDto messageDto);

    /**
     * 새로운 채팅방을 생성합니다.
     */
    ChatRoomDto.Response createRoom(ChatRoomDto.Request requestDto, String username);

    /**
     * 현재 로그인한 사용자의 모든 채팅방 목록을 조회합니다.
     */
    List<ChatRoomDto.Response> findAllRoomsByUser(String username);

    /**
     * 특정 채팅방의 이전 대화 내역을 모두 불러옵니다.
     */
    List<ChatMessageDto> loadChatHistory(Long roomId);

    /**
     * 특정 채팅방을 삭제합니다.
     */
    void deleteRoom(Long roomId, String username);
}
