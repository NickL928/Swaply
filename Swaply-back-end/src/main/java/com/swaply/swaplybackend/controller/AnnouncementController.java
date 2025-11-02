package com.swaply.swaplybackend.controller;

import com.swaply.swaplybackend.entity.Announcement;
import com.swaply.swaplybackend.repository.AnnouncementRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/announcements")
public class AnnouncementController {

    private final AnnouncementRepository repo;

    public AnnouncementController(AnnouncementRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Announcement> list() { return repo.findAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<Announcement> get(@PathVariable Long id) {
        return repo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
}

