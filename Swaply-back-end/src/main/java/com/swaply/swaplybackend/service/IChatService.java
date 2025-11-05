package com.swaply.swaplybackend.service;

import com.swaply.swaplybackend.dto.ChatMessageDto;
import com.swaply.swaplybackend.dto.ConversationDto;
import com.swaply.swaplybackend.entity.ChatMessage;

import java.util.List;

public interface IChatService {
    ChatMessage saveMessage(ChatMessageDto dto);
    List<ChatMessageDto> getThread(Long a, Long b);
    List<ConversationDto> getConversations(Long userId);
    void markRead(Long userId, Long peerId);
}
