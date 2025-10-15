package com.swaply.swaplybackend.service;

import com.swaply.swaplybackend.dto.PagedResponse;
import com.swaply.swaplybackend.dto.ThreadDto;
import com.swaply.swaplybackend.dto.ThreadReplyDto;
import com.swaply.swaplybackend.entity.Thread;
import com.swaply.swaplybackend.entity.ThreadReply;
import com.swaply.swaplybackend.entity.User;
import com.swaply.swaplybackend.enums.ThreadCategory;
import com.swaply.swaplybackend.repository.ThreadReplyRepository;
import com.swaply.swaplybackend.repository.ThreadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class ThreadService implements IThreadService {

    private final ThreadRepository threadRepository;
    private final ThreadReplyRepository replyRepository;
    private final IUserService userService;

    @Autowired
    public ThreadService(ThreadRepository threadRepository, ThreadReplyRepository replyRepository, IUserService userService) {
        this.threadRepository = threadRepository;
        this.replyRepository = replyRepository;
        this.userService = userService;
    }

    @Override
    public PagedResponse<ThreadDto> list(int page, int size, String q, String category) {
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), Math.min(Math.max(size, 1), 50));
        ThreadCategory cat = null;
        if (category != null && !category.isBlank()) {
            try {
                cat = ThreadCategory.valueOf(category.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ignored) {}
        }
        Page<Thread> results = threadRepository.search((q == null || q.isBlank()) ? null : q, cat, pageable);
        return new PagedResponse<>(
                results.getContent().stream().map(ThreadDto::from).collect(Collectors.toList()),
                page,
                size,
                results.getTotalElements()
        );
    }

    @Override
    public ThreadDto create(String title, String body, String category, String username) {
        if (title == null || title.isBlank() || body == null || body.isBlank()) {
            throw new IllegalArgumentException("Title and body are required");
        }
        User author = userService.getUserByUserName(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        Thread t = new Thread();
        t.setTitle(title.trim());
        t.setBody(body.trim());
        ThreadCategory cat = ThreadCategory.GENERAL;
        if (category != null) {
            try { cat = ThreadCategory.valueOf(category.toUpperCase(Locale.ROOT)); } catch (Exception ignored) {}
        }
        t.setCategory(cat);
        t.setAuthor(author);
        t.setLikesCount(0);
        t.setRepliesCount(0);
        t.setViewsCount(0);
        Thread saved = threadRepository.save(t);
        return ThreadDto.from(saved);
    }

    @Override
    public ThreadDto get(Long id, boolean incrementView) {
        Thread t = threadRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Thread not found: " + id));
        if (incrementView) {
            t.setViewsCount(t.getViewsCount() + 1);
            t = threadRepository.save(t);
        }
        return ThreadDto.from(t);
    }

    @Override
    public ThreadDto like(Long id) {
        Thread t = threadRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Thread not found: " + id));
        t.setLikesCount(Math.max(0, t.getLikesCount() + 1));
        return ThreadDto.from(threadRepository.save(t));
    }

    @Override
    public ThreadDto unlike(Long id) {
        Thread t = threadRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Thread not found: " + id));
        t.setLikesCount(Math.max(0, t.getLikesCount() - 1));
        return ThreadDto.from(threadRepository.save(t));
    }

    @Override
    public PagedResponse<ThreadReplyDto> listReplies(Long threadId, int page, int size) {
        Thread thread = threadRepository.findById(threadId)
                .orElseThrow(() -> new IllegalArgumentException("Thread not found: " + threadId));
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), Math.min(Math.max(size, 1), 100));
        Page<ThreadReply> replies = replyRepository.findByThreadOrderByCreatedAtAsc(thread, pageable);
        return new PagedResponse<>(
                replies.getContent().stream().map(ThreadReplyDto::from).collect(Collectors.toList()),
                page,
                size,
                replies.getTotalElements()
        );
    }

    @Override
    public ThreadReplyDto addReply(Long threadId, String body, String username) {
        if (body == null || body.isBlank()) {
            throw new IllegalArgumentException("Reply body is required");
        }
        Thread thread = threadRepository.findById(threadId)
                .orElseThrow(() -> new IllegalArgumentException("Thread not found: " + threadId));
        User author = userService.getUserByUserName(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        ThreadReply r = new ThreadReply();
        r.setThread(thread);
        r.setAuthor(author);
        r.setBody(body.trim());
        ThreadReply saved = replyRepository.save(r);
        // increment counter
        thread.setRepliesCount(Math.max(0, thread.getRepliesCount() + 1));
        threadRepository.save(thread);
        return ThreadReplyDto.from(saved);
    }
}
