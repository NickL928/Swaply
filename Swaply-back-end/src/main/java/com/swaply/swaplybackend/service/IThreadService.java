package com.swaply.swaplybackend.service;

import com.swaply.swaplybackend.dto.PagedResponse;
import com.swaply.swaplybackend.dto.ThreadDto;
import com.swaply.swaplybackend.dto.ThreadReplyDto;

public interface IThreadService {
    PagedResponse<ThreadDto> list(int page, int size, String q, String category);
    ThreadDto create(String title, String body, String category, String username);
    ThreadDto get(Long id, boolean incrementView);
    ThreadDto like(Long id);
    ThreadDto unlike(Long id);

    PagedResponse<ThreadReplyDto> listReplies(Long threadId, int page, int size);
    ThreadReplyDto addReply(Long threadId, String body, String username);
}
