package com.swaply.swaplybackend.repository;

import com.swaply.swaplybackend.entity.ChatMessage;
import com.swaply.swaplybackend.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // Full ordered thread between two users
    @Query("SELECT m FROM ChatMessage m WHERE (m.fromUser.userId = :a AND m.toUser.userId = :b) OR (m.fromUser.userId = :b AND m.toUser.userId = :a) ORDER BY m.timestamp ASC")
    List<ChatMessage> findThread(@Param("a") Long a, @Param("b") Long b);

    // Distinct peers this user has messages with (either direction)
    @Query("SELECT DISTINCT CASE WHEN m.fromUser.userId = :userId THEN m.toUser ELSE m.fromUser END FROM ChatMessage m WHERE m.fromUser.userId = :userId OR m.toUser.userId = :userId")
    List<User> findPeers(@Param("userId") Long userId);

    // Unread messages count from a specific peer
    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.toUser.userId = :userId AND m.fromUser.userId = :peerId AND m.readFlag = false")
    long countUnreadFromPeer(@Param("userId") Long userId, @Param("peerId") Long peerId);

    // Last message between two users (use Pageable for top-1)
    @Query("SELECT m FROM ChatMessage m WHERE (m.fromUser.userId = :userId AND m.toUser.userId = :peerId) OR (m.fromUser.userId = :peerId AND m.toUser.userId = :userId) ORDER BY m.timestamp DESC")
    List<ChatMessage> findLastBetween(@Param("userId") Long userId, @Param("peerId") Long peerId, Pageable pageable);

    // Mark messages as read from a peer
    @Modifying
    @Query("UPDATE ChatMessage m SET m.readFlag = true WHERE m.toUser.userId = :userId AND m.fromUser.userId = :peerId AND m.readFlag = false")
    int markReadFromPeer(@Param("userId") Long userId, @Param("peerId") Long peerId);
}
