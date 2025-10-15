package com.swaply.swaplybackend.controller;

import com.swaply.swaplybackend.dto.CreateReplyRequest;
import com.swaply.swaplybackend.dto.CreateThreadRequest;
import com.swaply.swaplybackend.dto.PagedResponse;
import com.swaply.swaplybackend.dto.ThreadDto;
import com.swaply.swaplybackend.dto.ThreadReplyDto;
import com.swaply.swaplybackend.service.IThreadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/threads")
public class ThreadController {

    @Autowired
    private IThreadService threadService;

    @GetMapping("")
    public ResponseEntity<PagedResponse<ThreadDto>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category
    ) {
        return ResponseEntity.ok(threadService.list(page, size, q, category));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id, @RequestParam(name = "inc", defaultValue = "true") boolean inc) {
        try {
            return ResponseEntity.ok(threadService.get(id, inc));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        }
    }

    @PostMapping("")
    public ResponseEntity<?> create(@RequestBody CreateThreadRequest req, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            String username = auth.getName();
            ThreadDto dto = threadService.create(req.getTitle(), req.getBody(), req.getCategory(), username);
            return ResponseEntity.status(HttpStatus.CREATED).body(dto);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<?> like(@PathVariable Long id, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            return ResponseEntity.ok(threadService.like(id));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        }
    }

    @PostMapping("/{id}/unlike")
    public ResponseEntity<?> unlike(@PathVariable Long id, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            return ResponseEntity.ok(threadService.unlike(id));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        }
    }

    // Replies
    @GetMapping("/{id}/replies")
    public ResponseEntity<?> listReplies(@PathVariable Long id,
                                         @RequestParam(defaultValue = "1") int page,
                                         @RequestParam(defaultValue = "20") int size) {
        try {
            return ResponseEntity.ok(threadService.listReplies(id, page, size));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        }
    }

    @PostMapping("/{id}/replies")
    public ResponseEntity<?> addReply(@PathVariable Long id,
                                      @RequestBody CreateReplyRequest req,
                                      Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            String username = auth.getName();
            ThreadReplyDto dto = threadService.addReply(id, req.getBody(), username);
            return ResponseEntity.status(HttpStatus.CREATED).body(dto);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }
}
