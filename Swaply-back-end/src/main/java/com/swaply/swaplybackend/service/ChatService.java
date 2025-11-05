package com.swaply.swaplybackend.service;

import com.swaply.swaplybackend.dto.ChatMessageDto;
import com.swaply.swaplybackend.dto.ConversationDto;
import com.swaply.swaplybackend.entity.ChatMessage;
import com.swaply.swaplybackend.entity.User;
import com.swaply.swaplybackend.repository.ChatMessageRepository;
import com.swaply.swaplybackend.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatService implements IChatService {

    private final ChatMessageRepository chatRepo;
    private final UserRepository userRepo;

    public ChatService(ChatMessageRepository chatRepo, UserRepository userRepo) {
        this.chatRepo = chatRepo;
        this.userRepo = userRepo;
    }

    @Override
    @Transactional
    public ChatMessage saveMessage(ChatMessageDto dto) {
        User from = userRepo.findById(dto.getFromUserId()).orElseThrow();
        User to = userRepo.findById(dto.getToUserId()).orElseThrow();
        ChatMessage m = new ChatMessage();
        m.setFromUser(from);
        m.setToUser(to);
        m.setContent(dto.getContent());
        m.setTimestamp(dto.getTimestamp() != null ? dto.getTimestamp() : LocalDateTime.now());
        m.setReadFlag(false);
        return chatRepo.save(m);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageDto> getThread(Long a, Long b) {
        return chatRepo.findThread(a,b).stream().map(m -> {
            ChatMessageDto d = new ChatMessageDto();
            d.setFromUserId(m.getFromUser().getUserId());
            d.setToUserId(m.getToUser().getUserId());
            d.setContent(m.getContent());
            d.setTimestamp(m.getTimestamp());
            return d;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConversationDto> getConversations(Long userId) {
        List<User> peers = chatRepo.findPeers(userId);
        List<ConversationDto> list = new ArrayList<>();
        for (User peer : peers) {
            var lastList = chatRepo.findLastBetween(userId, peer.getUserId(), PageRequest.of(0,1));
            ChatMessage last = lastList.isEmpty() ? null : lastList.get(0);
            ConversationDto c = new ConversationDto();
            c.setPeerId(peer.getUserId());
            c.setPeerName(peer.getUserName());
            c.setPeerAvatarUrl(peer.getProfileImageUrl());
            if (last != null) {
                c.setLastContent(last.getContent());
                c.setLastTimestamp(last.getTimestamp());
            }
            c.setUnreadCount(chatRepo.countUnreadFromPeer(userId, peer.getUserId()));
            list.add(c);
        }
        list.sort(Comparator.comparing(ConversationDto::getLastTimestamp, Comparator.nullsFirst(Comparator.naturalOrder())).reversed());
        return list;
    }

    @Override
    @Transactional
    public void markRead(Long userId, Long peerId) {
        chatRepo.markReadFromPeer(userId, peerId);
    }
}
