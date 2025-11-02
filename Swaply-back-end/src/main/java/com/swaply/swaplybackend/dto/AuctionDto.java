package com.swaply.swaplybackend.dto;

import com.swaply.swaplybackend.enums.AuctionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AuctionDto {
    private Long auctionId;
    private Long listingId;
    private String title;
    private String imageUrl;
    private BigDecimal startingPrice;
    private BigDecimal currentPrice;
    private BigDecimal minIncrement;
    private AuctionStatus status;
    private Long sellerId;
    private String sellerUsername;
    private String sellerProfileImageUrl;
    private Long highestBidderId;
    private String highestBidderUsername;
    private LocalDateTime endTime;

    public Long getAuctionId() { return auctionId; }
    public void setAuctionId(Long auctionId) { this.auctionId = auctionId; }
    public Long getListingId() { return listingId; }
    public void setListingId(Long listingId) { this.listingId = listingId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public BigDecimal getStartingPrice() { return startingPrice; }
    public void setStartingPrice(BigDecimal startingPrice) { this.startingPrice = startingPrice; }
    public BigDecimal getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(BigDecimal currentPrice) { this.currentPrice = currentPrice; }
    public BigDecimal getMinIncrement() { return minIncrement; }
    public void setMinIncrement(BigDecimal minIncrement) { this.minIncrement = minIncrement; }
    public AuctionStatus getStatus() { return status; }
    public void setStatus(AuctionStatus status) { this.status = status; }
    public Long getSellerId() { return sellerId; }
    public void setSellerId(Long sellerId) { this.sellerId = sellerId; }
    public String getSellerUsername() { return sellerUsername; }
    public void setSellerUsername(String sellerUsername) { this.sellerUsername = sellerUsername; }
    public String getSellerProfileImageUrl() { return sellerProfileImageUrl; }
    public void setSellerProfileImageUrl(String sellerProfileImageUrl) { this.sellerProfileImageUrl = sellerProfileImageUrl; }
    public Long getHighestBidderId() { return highestBidderId; }
    public void setHighestBidderId(Long highestBidderId) { this.highestBidderId = highestBidderId; }
    public String getHighestBidderUsername() { return highestBidderUsername; }
    public void setHighestBidderUsername(String highestBidderUsername) { this.highestBidderUsername = highestBidderUsername; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
}
