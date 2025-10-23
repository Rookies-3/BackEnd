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

    @Override
    public void processMessage(ChatMessageDto messageDto) {
        log.info("Processing user message for room: {}", messageDto.getRoomId());
        ChatRoom chatRoom = chatRoomRepository.findById(messageDto.getRoomId())
                .orElseThrow(() -> new IllegalArgumentException("Chat room not found: " + messageDto.getRoomId()));

        // 1. 사용자의 메시지를 DB에 저장합니다.
        ChatMessage userMessage = ChatMessage.builder()
                .chatRoom(chatRoom)
                .sender(messageDto.getSender())
                .message(messageDto.getMessage())
                .build();
        chatMessageRepository.save(userMessage);

        // 2. AI의 응답을 비동기적으로 요청하고, 응답이 오면 후속 작업을 처리합니다.
        aiService.getAIResponse(messageDto.getMessage())
            .thenAcceptAsync(aiResponse -> {
                log.info("AI response received for room: {}. Sending to subscribers.", messageDto.getRoomId());
                // 3. AI의 응답 메시지를 생성하고 DB에 저장합니다.
                ChatMessage aiChatMessage = ChatMessage.builder()
                        .chatRoom(chatRoom)
                        .sender("AI")
                        .message(aiResponse)
                        .build();
                chatMessageRepository.save(aiChatMessage);

                // 4. AI의 응답을 DTO로 변환하여 웹소켓 구독자에게 전송합니다.
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
                .roomName("테스트 방")
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
