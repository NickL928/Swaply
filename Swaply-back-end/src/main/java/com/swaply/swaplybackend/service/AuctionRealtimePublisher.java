package com.swaply.swaplybackend.service;

import com.swaply.swaplybackend.dto.AuctionBidUpdateDto;
import com.swaply.swaplybackend.dto.AuctionDto;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class AuctionRealtimePublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public AuctionRealtimePublisher(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Broadcasts a lightweight update to everyone watching an auction.
     * Destination: /topic/auctions/{auctionId}
     */
    public void publishBidUpdate(AuctionDto auction) {
        if (auction == null || auction.getAuctionId() == null) return;

        AuctionBidUpdateDto dto = new AuctionBidUpdateDto();
        dto.setAuctionId(auction.getAuctionId());
        dto.setCurrentPrice(auction.getCurrentPrice());
        dto.setHighestBidderId(auction.getHighestBidderId());
        dto.setHighestBidderUsername(auction.getHighestBidderUsername());
        dto.setEndTime(auction.getEndTime());

        messagingTemplate.convertAndSend("/topic/auctions/" + auction.getAuctionId(), dto);
    }
}

