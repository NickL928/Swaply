package com.swaply.swaplybackend.repository;

import com.swaply.swaplybackend.entity.Thread;
import com.swaply.swaplybackend.entity.ThreadReply;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ThreadReplyRepository extends JpaRepository<ThreadReply, Long> {
    Page<ThreadReply> findByThreadOrderByCreatedAtAsc(Thread thread, Pageable pageable);
    long countByThread(Thread thread);
}

