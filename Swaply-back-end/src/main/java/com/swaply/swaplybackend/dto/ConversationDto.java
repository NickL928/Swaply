package com.swaply.swaplybackend.dto;

import java.time.LocalDateTime;

public class ConversationDto {
    private Long peerId;
    private String peerName;
    private String peerAvatarUrl;
    private String lastContent;
    private LocalDateTime lastTimestamp;
    private long unreadCount;

    public Long getPeerId() { return peerId; }
    public void setPeerId(Long peerId) { this.peerId = peerId; }
    public String getPeerName() { return peerName; }
    public void setPeerName(String peerName) { this.peerName = peerName; }
    public String getPeerAvatarUrl() { return peerAvatarUrl; }
    public void setPeerAvatarUrl(String peerAvatarUrl) { this.peerAvatarUrl = peerAvatarUrl; }
    public String getLastContent() { return lastContent; }
    public void setLastContent(String lastContent) { this.lastContent = lastContent; }
    public LocalDateTime getLastTimestamp() { return lastTimestamp; }
    public void setLastTimestamp(LocalDateTime lastTimestamp) { this.lastTimestamp = lastTimestamp; }
    public long getUnreadCount() { return unreadCount; }
    public void setUnreadCount(long unreadCount) { this.unreadCount = unreadCount; }
}

