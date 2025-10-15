package com.swaply.swaplybackend.dto;

import com.swaply.swaplybackend.entity.Thread;

import java.time.LocalDateTime;
import java.util.Map;

public class ThreadDto {
    private Long id;
    private String title;
    private String body;
    private String category;
    private Author author;
    private LocalDateTime createdAt;
    private Stats stats;

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

    public static class Stats {
        private int replies;
        private int likes;
        private Integer views;
        public Stats() {}
        public Stats(int replies, int likes, Integer views) { this.replies = replies; this.likes = likes; this.views = views; }
        public int getReplies() { return replies; }
        public void setReplies(int replies) { this.replies = replies; }
        public int getLikes() { return likes; }
        public void setLikes(int likes) { this.likes = likes; }
        public Integer getViews() { return views; }
        public void setViews(Integer views) { this.views = views; }
    }

    public static ThreadDto from(Thread t) {
        ThreadDto dto = new ThreadDto();
        dto.setId(t.getThreadId());
        dto.setTitle(t.getTitle());
        dto.setBody(t.getBody());
        dto.setCategory(t.getCategory() != null ? t.getCategory().name() : null);
        dto.setAuthor(new Author(
                t.getAuthor() != null ? t.getAuthor().getUserId() : null,
                t.getAuthor() != null ? t.getAuthor().getUserName() : null
        ));
        dto.setCreatedAt(t.getCreatedAt());
        dto.setStats(new Stats(t.getRepliesCount(), t.getLikesCount(), t.getViewsCount()));
        return dto;
    }

    // getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public Author getAuthor() { return author; }
    public void setAuthor(Author author) { this.author = author; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public Stats getStats() { return stats; }
    public void setStats(Stats stats) { this.stats = stats; }
}

