package com.swaply.swaplybackend.dto;

import com.swaply.swaplybackend.entity.ThreadReply;

import java.time.LocalDateTime;

public class ThreadReplyDto {
    private Long id;
    private String body;
    private Author author;
    private LocalDateTime createdAt;

    public static class Author {
        private Long id;
        private String name;
        public Author() {}
        public Author(Long id, String name) { this.id = id; this.name = name; }
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    public static ThreadReplyDto from(ThreadReply r) {
        ThreadReplyDto dto = new ThreadReplyDto();
        dto.setId(r.getReplyId());
        dto.setBody(r.getBody());
        dto.setAuthor(new Author(
                r.getAuthor() != null ? r.getAuthor().getUserId() : null,
                r.getAuthor() != null ? r.getAuthor().getUserName() : null
        ));
        dto.setCreatedAt(r.getCreatedAt());
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public Author getAuthor() { return author; }
    public void setAuthor(Author author) { this.author = author; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

