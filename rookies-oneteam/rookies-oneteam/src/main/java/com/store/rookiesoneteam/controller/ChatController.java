package com.store.rookiesoneteam.controller;

import com.store.rookiesoneteam.dto.ChatMessageDto;
import com.store.rookiesoneteam.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessageSendingOperations messagingTemplate;
    private final ChatService chatService;

    @MessageMapping("/chat/message")
    public void message(ChatMessageDto message) {
//        // 사용자가 입장했을 때의 알림 메시지 처리
//        if (MessageType.ENTER.equals(message.getType())) {
//            message.setMessage(message.getSender() + "님이 입장하셨습니다.");
//            messagingTemplate.convertAndSend("/sub/chat/room/" + message.getRoomId(), message);
//            return; // 입장 메시지는 저장하지 않고 알리기만 함
//        }
// 백두현: 방 입장에 따른 처리는 현 시스템에 필요하지 않기에 주석처리함.

        // 사용자의 일반 대화 메시지 처리
        // 1. 메시지를 DB에 저장하고 AI 응답을 요청하는 서비스를 호출
        chatService.processMessage(message);

        // 2. 사용자가 보낸 메시지를 즉시 구독자들에게 전송
        messagingTemplate.convertAndSend("/sub/chat/room/" + message.getRoomId(), message);
    }
}
