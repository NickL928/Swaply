package com.swaply.swaplybackend.service;

import com.swaply.swaplybackend.dto.ChatMessageDto;
import com.swaply.swaplybackend.dto.ConversationDto;
import com.swaply.swaplybackend.entity.ChatMessage;
import com.swaply.swaplybackend.entity.User;
import com.swaply.swaplybackend.repository.ChatMessageRepository;
import com.swaply.swaplybackend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock ChatMessageRepository chatRepo;
    @Mock UserRepository userRepo;

    @InjectMocks ChatService chatService;

    private User nick;
    private User buyer;

    @BeforeEach
    void init() {
        nick = new User();
        nick.setUserId(1L);
        nick.setUserName("nick");
        buyer = new User();
        buyer.setUserId(2L);
        buyer.setUserName("buyer");
    }

    @Test
    void saveMessage_persistsEntityWithTimestamp() {
        ChatMessageDto dto = new ChatMessageDto();
        dto.setFromUserId(1L);
        dto.setToUserId(2L);
        dto.setContent("Hello");

        ChatMessage stored = new ChatMessage();
        stored.setFromUser(nick);
        stored.setToUser(buyer);
        stored.setContent("Hello");
        stored.setTimestamp(LocalDateTime.now());

        when(userRepo.findById(1L)).thenReturn(Optional.of(nick));
        when(userRepo.findById(2L)).thenReturn(Optional.of(buyer));
        when(chatRepo.save(any(ChatMessage.class))).thenReturn(stored);

        ChatMessage saved = chatService.saveMessage(dto);

        assertThat(saved.getContent()).isEqualTo("Hello");
        verify(chatRepo).save(argThat(msg -> msg.getFromUser().equals(nick) && msg.getToUser().equals(buyer)));
    }

    @Test
    void getThread_mapsEntitiesToDtos() {
        ChatMessage one = new ChatMessage();
        one.setFromUser(nick);
        one.setToUser(buyer);
        one.setContent("A");
        one.setTimestamp(LocalDateTime.now().minusMinutes(2));

        ChatMessage two = new ChatMessage();
        two.setFromUser(buyer);
        two.setToUser(nick);
        two.setContent("B");
        two.setTimestamp(LocalDateTime.now().minusMinutes(1));

        when(chatRepo.findThread(1L, 2L)).thenReturn(List.of(one, two));

        List<ChatMessageDto> thread = chatService.getThread(1L, 2L);

        assertThat(thread).hasSize(2);
        assertThat(thread.get(0).getContent()).isEqualTo("A");
        assertThat(thread.get(1).getFromUserId()).isEqualTo(2L);
    }

    @Test
    void getConversations_sortsByLastMessageAndCountsUnread() {
        when(chatRepo.findPeers(1L)).thenReturn(List.of(buyer));
        ChatMessage last = new ChatMessage();
        last.setFromUser(buyer);
        last.setToUser(nick);
        last.setContent("Ping");
        last.setTimestamp(LocalDateTime.now());
        when(chatRepo.findLastBetween(eq(1L), eq(2L), any(PageRequest.class))).thenReturn(List.of(last));
        when(chatRepo.countUnreadFromPeer(1L, 2L)).thenReturn(3L);

        List<ConversationDto> conversations = chatService.getConversations(1L);

        assertThat(conversations).singleElement().satisfies(c -> {
            assertThat(c.getPeerId()).isEqualTo(2L);
            assertThat(c.getLastContent()).isEqualTo("Ping");
            assertThat(c.getUnreadCount()).isEqualTo(3L);
        });
    }

    @Test
    void markRead_delegatesToRepository() {
        chatService.markRead(1L, 2L);
        verify(chatRepo).markReadFromPeer(1L, 2L);
    }
}

