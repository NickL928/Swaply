package com.swaply.swaplybackend.controller;

import com.swaply.swaplybackend.dto.ChatMessageDto;
import com.swaply.swaplybackend.dto.ConversationDto;
import com.swaply.swaplybackend.service.IChatService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.http.ResponseEntity;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RestController
@RequestMapping
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final IChatService chatService;

    public ChatController(SimpMessagingTemplate messagingTemplate, IChatService chatService) {
        this.messagingTemplate = messagingTemplate;
        this.chatService = chatService;
    }

    // Client sends to /app/chat.private
    @MessageMapping("/chat.private")
    public void sendPrivate(@Payload ChatMessageDto message, Principal principal) {
        if (message == null || message.getToUserId() == null) return;
        if (principal == null) return;

        // Ensure sender id from Principal (we set Principal.name = userId)
        try {
            Long principalUserId = Long.parseLong(principal.getName());
            if (message.getFromUserId() == null || !principalUserId.equals(message.getFromUserId())) {
                message.setFromUserId(principalUserId);
            }
        } catch (Exception ignored) {}

        message.setTimestamp(LocalDateTime.now());
        // persist
        chatService.saveMessage(message);

        // Deliver to recipient and echo to sender
        messagingTemplate.convertAndSendToUser(String.valueOf(message.getToUserId()), "/queue/messages", message);
        messagingTemplate.convertAndSendToUser(String.valueOf(message.getFromUserId()), "/queue/messages", message);
    }

    @GetMapping("/api/chat/thread")
    public List<ChatMessageDto> getThread(@RequestParam("a") Long a, @RequestParam("b") Long b) {
        return chatService.getThread(a,b);
    }

    @GetMapping("/api/chat/conversations")
    public ResponseEntity<List<ConversationDto>> conversations(Principal principal) {
        if (principal == null) return ResponseEntity.status(401).build();
        Long userId;
        try { userId = Long.parseLong(principal.getName()); }
        catch (Exception e) { return ResponseEntity.status(401).build(); }
        return ResponseEntity.ok(chatService.getConversations(userId));
    }

    @PostMapping("/api/chat/mark-read")
    public ResponseEntity<Void> markRead(@RequestParam("userId") Long userId, @RequestParam("peerId") Long peerId) {
        chatService.markRead(userId, peerId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/chat/send")
    public ResponseEntity<ChatMessageDto> sendViaRest(@RequestBody ChatMessageDto message, Principal principal) {
        if (message == null || message.getToUserId() == null) return ResponseEntity.badRequest().build();
        if (principal == null) return ResponseEntity.status(401).build();
        try {
            Long principalUserId = Long.parseLong(principal.getName());
            message.setFromUserId(principalUserId);
        } catch (Exception ignored) {}
        message.setTimestamp(LocalDateTime.now());
        var saved = chatService.saveMessage(message);
        ChatMessageDto dto = new ChatMessageDto();
        dto.setFromUserId(saved.getFromUser().getUserId());
        dto.setToUserId(saved.getToUser().getUserId());
        dto.setContent(saved.getContent());
        dto.setTimestamp(saved.getTimestamp());
        // Also push via WS if possible
        messagingTemplate.convertAndSendToUser(String.valueOf(dto.getToUserId()), "/queue/messages", dto);
        messagingTemplate.convertAndSendToUser(String.valueOf(dto.getFromUserId()), "/queue/messages", dto);
        return ResponseEntity.ok(dto);
    }
}
