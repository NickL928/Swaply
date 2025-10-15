package com.swaply.swaplybackend.repository;

import com.swaply.swaplybackend.entity.Thread;
import com.swaply.swaplybackend.enums.ThreadCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ThreadRepository extends JpaRepository<Thread, Long> {

    @Query("SELECT t FROM CommunityThread t " +
            "WHERE (:category IS NULL OR t.category = :category) " +
            "AND ( :q IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :q, '%')) " +
            "OR LOWER(CONCAT('', t.body)) LIKE LOWER(CONCAT('%', :q, '%')) ) " +
            "ORDER BY t.createdAt DESC")
    Page<Thread> search(@Param("q") String q,
                        @Param("category") ThreadCategory category,
                        Pageable pageable);
}
