package com.swaply.swaplybackend.entity;

import com.swaply.swaplybackend.enums.AuctionStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_auction")
public class Auction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "auction_id")
    private Long auctionId;

    @OneToOne
    @JoinColumn(name = "listing_id", nullable = false)
    private Listing listing;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AuctionStatus status = AuctionStatus.ACTIVE;

    @Column(name = "starting_price", nullable = false)
    private BigDecimal startingPrice;

    @Column(name = "current_price", nullable = false)
    private BigDecimal currentPrice;

    @Column(name = "min_increment", nullable = false)
    private BigDecimal minIncrement;

    @ManyToOne
    @JoinColumn(name = "highest_bidder_id")
    private User highestBidder;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @CreationTimestamp
    private LocalDateTime createdDate;

    @UpdateTimestamp
    private LocalDateTime updatedDate;

    public Auction() {}

    public Long getAuctionId() { return auctionId; }
    public void setAuctionId(Long auctionId) { this.auctionId = auctionId; }

    public Listing getListing() { return listing; }
    public void setListing(Listing listing) { this.listing = listing; }

    public AuctionStatus getStatus() { return status; }
    public void setStatus(AuctionStatus status) { this.status = status; }

    public BigDecimal getStartingPrice() { return startingPrice; }
    public void setStartingPrice(BigDecimal startingPrice) { this.startingPrice = startingPrice; }

    public BigDecimal getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(BigDecimal currentPrice) { this.currentPrice = currentPrice; }

    public BigDecimal getMinIncrement() { return minIncrement; }
    public void setMinIncrement(BigDecimal minIncrement) { this.minIncrement = minIncrement; }

    public User getHighestBidder() { return highestBidder; }
    public void setHighestBidder(User highestBidder) { this.highestBidder = highestBidder; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public LocalDateTime getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(LocalDateTime updatedDate) { this.updatedDate = updatedDate; }
}