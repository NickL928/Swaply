package com.swaply.swaplybackend.controller;

import com.swaply.swaplybackend.dto.ChatMessageDto;
import com.swaply.swaplybackend.dto.ConversationDto;
import com.swaply.swaplybackend.service.IChatService;
import com.swaply.swaplybackend.service.UserService;
import com.swaply.swaplybackend.entity.User;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RestController
@RequestMapping
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final IChatService chatService;
    private final UserService userService;

    public ChatController(SimpMessagingTemplate messagingTemplate, IChatService chatService, UserService userService) {
        this.messagingTemplate = messagingTemplate;
        this.chatService = chatService;
        this.userService = userService;
    }

    private User requirePrincipal(Principal principal) {
        if (principal == null || principal.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        return userService.getUserByUserName(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
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
    public ResponseEntity<List<ChatMessageDto>> getThread(@RequestParam("a") Long a,
                                                          @RequestParam("b") Long b,
                                                          Principal principal) {
        User current = requirePrincipal(principal);
        Long userId = current.getUserId();
        if (!userId.equals(a) && !userId.equals(b)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return ResponseEntity.ok(chatService.getThread(a, b));
    }

    @GetMapping("/api/chat/conversations")
    public ResponseEntity<List<ConversationDto>> conversations(Principal principal) {
        User current = requirePrincipal(principal);
        return ResponseEntity.ok(chatService.getConversations(current.getUserId()));
    }

    @PostMapping("/api/chat/mark-read")
    public ResponseEntity<Void> markRead(@RequestParam("peerId") Long peerId, Principal principal) {
        User current = requirePrincipal(principal);
        chatService.markRead(current.getUserId(), peerId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/chat/send")
    public ResponseEntity<ChatMessageDto> sendViaRest(@RequestBody ChatMessageDto message, Principal principal) {
        User current = requirePrincipal(principal);
        if (message == null || message.getToUserId() == null) {
            return ResponseEntity.badRequest().build();
        }
        message.setFromUserId(current.getUserId());
        message.setTimestamp(LocalDateTime.now());
        var saved = chatService.saveMessage(message);
        ChatMessageDto dto = new ChatMessageDto();
        dto.setFromUserId(saved.getFromUser().getUserId());
        dto.setToUserId(saved.getToUser().getUserId());
        dto.setContent(saved.getContent());
        dto.setTimestamp(saved.getTimestamp());
        messagingTemplate.convertAndSendToUser(String.valueOf(dto.getToUserId()), "/queue/messages", dto);
        messagingTemplate.convertAndSendToUser(String.valueOf(dto.getFromUserId()), "/queue/messages", dto);
        return ResponseEntity.ok(dto);
    }
}
