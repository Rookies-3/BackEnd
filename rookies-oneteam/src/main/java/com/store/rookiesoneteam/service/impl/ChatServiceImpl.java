package com.store.rookiesoneteam.service.impl;

import com.store.rookiesoneteam.domain.entity.ChatMessage;
import com.store.rookiesoneteam.domain.entity.ChatRoom;
import com.store.rookiesoneteam.domain.entity.User;
import com.store.rookiesoneteam.dto.ChatMessageDto;
import com.store.rookiesoneteam.dto.ChatRoomDto;
import com.store.rookiesoneteam.repository.ChatMessageRepository;
import com.store.rookiesoneteam.repository.ChatRoomRepository;
import com.store.rookiesoneteam.repository.UserRepository;
import com.store.rookiesoneteam.service.AIService;
import com.store.rookiesoneteam.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ChatServiceImpl implements ChatService {

    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final AIService aiService;
    private final SimpMessageSendingOperations messagingTemplate;

    // ⭐️ 수정: 클래스 레벨의 상수로 선언하여 모든 메서드에서 접근 가능하도록 변경
    private static final String DEFAULT_ROOM_NAME = "테스트 방";

    @Override
    public void processMessage(ChatMessageDto messageDto) {
        log.info("Processing user message for room: {}", messageDto.getRoomId());
        ChatRoom chatRoom = chatRoomRepository.findById(messageDto.getRoomId())
                .orElseThrow(() -> new IllegalArgumentException("Chat room not found: " + messageDto.getRoomId()));

        // --- ⭐️ 실시간 업데이트 로직 시작 ---
        // 기존의 지역 변수 선언 (final String DEFAULT_ROOM_NAME = "테스트 방";) 삭제
        boolean isRoomNameUpdated = false;

        // 메시지 타입이 'TALK'이고, 현재 방 제목이 기본값일 경우에만 업데이트
        if (messageDto.getType() == ChatMessageDto.MessageType.TALK && chatRoom.getRoomName().equals(DEFAULT_ROOM_NAME)) {
            String newRoomName = messageDto.getMessage();

            // 제목이 너무 길 경우 20자까지만 자르고 "..." 추가
            if (newRoomName.length() > 20) {
                newRoomName = newRoomName.substring(0, 20) + "...";
            }

            chatRoom.setRoomName(newRoomName); // ChatRoom.java에 추가된 Setter를 사용하여 제목 변경
            chatRoomRepository.save(chatRoom); // 변경 내용 DB에 반영

            log.info("채팅방 제목이 첫 메시지로 업데이트되었습니다: RoomId={}, NewName={}", chatRoom.getId(), newRoomName);
            isRoomNameUpdated = true;
        }
        // --- ⭐️ 실시간 업데이트 로직 추가 끝 ---

        // 1. 사용자의 메시지를 DB에 저장합니다.
        ChatMessage userMessage = ChatMessage.builder()
                .chatRoom(chatRoom)
                .sender(messageDto.getSender())
                .message(messageDto.getMessage())
                .build();
        chatMessageRepository.save(userMessage);

        // 제목 변경이 있었다면 웹소켓으로 알림 전송
        if (isRoomNameUpdated) {
            ChatRoomDto.Response roomUpdateDto = ChatRoomDto.Response.fromEntity(chatRoom);
            // 모든 클라이언트의 채팅방 목록 업데이트를 위한 공통 토픽으로 전송
            // 이 토픽을 프런트엔드에서 구독하여 목록을 업데이트해야 합니다.
            messagingTemplate.convertAndSend("/sub/room-update", roomUpdateDto);
            log.info("웹소켓으로 채팅방 제목 업데이트 알림 전송: RoomId={}", chatRoom.getId());
        }

        // 3. AI의 응답을 비동기적으로 요청하고, 응답이 오면 후속 작업을 처리합니다.
        aiService.getAIResponse(messageDto)
                .thenAcceptAsync(aiResponse -> {
                    log.info("AI response received for room: {}. Sending to subscribers.", messageDto.getRoomId());
                    // 4. AI의 응답 메시지를 생성하고 DB에 저장합니다.
                    ChatMessage aiChatMessage = ChatMessage.builder()
                            .chatRoom(chatRoom)
                            .sender("AI")
                            .message(aiResponse)
                            .build();
                    chatMessageRepository.save(aiChatMessage);

                    // 5. AI의 응답을 DTO로 변환하여 웹소켓 구독자에게 전송합니다.
                    ChatMessageDto aiMessageDto = new ChatMessageDto(
                            ChatMessageDto.MessageType.TALK,
                            messageDto.getRoomId(),
                            "AI",
                            aiResponse,
                            LocalDateTime.now()
                    );
                    messagingTemplate.convertAndSend("/sub/chat/room/" + aiMessageDto.getRoomId(), aiMessageDto);
                });
    }

    @Override
    public ChatRoomDto.Response createRoom(ChatRoomDto.Request requestDto, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        ChatRoom chatRoom = ChatRoom.builder()
                .user(user)
                .roomName(DEFAULT_ROOM_NAME) // ⭐️ 수정: 상수를 사용하여 일관성 유지
                //.roomName(requestDto.getRoomName())
                .build();

        ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);
        return ChatRoomDto.Response.fromEntity(savedChatRoom);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatRoomDto.Response> findAllRoomsByUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return chatRoomRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(ChatRoomDto.Response::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageDto> loadChatHistory(Long roomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Chat room not found: " + roomId));

        return chatMessageRepository.findAllByChatRoomOrderByCreatedAtAsc(chatRoom).stream()
                .map(chatMessage -> new ChatMessageDto(
                        ChatMessageDto.MessageType.TALK,
                        chatMessage.getChatRoom().getId(),
                        chatMessage.getSender(),
                        chatMessage.getMessage(),
                        chatMessage.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteRoom(Long roomId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Chat room not found: " + roomId));

        // 방의 소유자만 삭제할 수 있도록 권한을 확인합니다.
        if (!chatRoom.getUser().equals(user)) {
            throw new AccessDeniedException("You do not have permission to delete this chat room.");
        }

        chatRoomRepository.delete(chatRoom);
    }
}