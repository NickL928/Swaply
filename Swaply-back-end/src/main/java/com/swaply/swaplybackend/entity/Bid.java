package com.swaply.swaplybackend.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_bid")
public class Bid {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bid_id")
    private Long bidId;

    @ManyToOne
    @JoinColumn(name = "auction_id", nullable = false)
    private Auction auction;

    @ManyToOne
    @JoinColumn(name = "bidder_id", nullable = false)
    private User bidder;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @CreationTimestamp
    private LocalDateTime createdDate;

    public Bid() {}

    public Long getBidId() { return bidId; }
    public void setBidId(Long bidId) { this.bidId = bidId; }

    public Auction getAuction() { return auction; }
    public void setAuction(Auction auction) { this.auction = auction; }

    public User getBidder() { return bidder; }
    public void setBidder(User bidder) { this.bidder = bidder; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
}

