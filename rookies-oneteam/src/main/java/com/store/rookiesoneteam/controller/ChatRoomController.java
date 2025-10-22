package com.store.rookiesoneteam.controller;

import com.store.rookiesoneteam.dto.ChatMessageDto;
import com.store.rookiesoneteam.dto.ChatRoomDto;
import com.store.rookiesoneteam.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatRoomController {

    private final ChatService chatService;

    @PostMapping("/room")
    public ResponseEntity<ChatRoomDto.Response> createRoom(@RequestBody ChatRoomDto.Request requestDto, @AuthenticationPrincipal UserDetails userDetails) {
        ChatRoomDto.Response createdRoom = chatService.createRoom(requestDto, userDetails.getUsername());
        return ResponseEntity.ok(createdRoom);
    }

    @GetMapping("/rooms")
    public ResponseEntity<List<ChatRoomDto.Response>> findAllRooms(@AuthenticationPrincipal UserDetails userDetails) {
        List<ChatRoomDto.Response> rooms = chatService.findAllRoomsByUser(userDetails.getUsername());
        return ResponseEntity.ok(rooms);
    }

    @GetMapping("/room/{roomId}/messages")
    public ResponseEntity<List<ChatMessageDto>> loadChatHistory(@PathVariable Long roomId) {
        List<ChatMessageDto> history = chatService.loadChatHistory(roomId);
        return ResponseEntity.ok(history);
    }

    @DeleteMapping("/room/{roomId}")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long roomId, @AuthenticationPrincipal UserDetails userDetails) {
        chatService.deleteRoom(roomId, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }
}
