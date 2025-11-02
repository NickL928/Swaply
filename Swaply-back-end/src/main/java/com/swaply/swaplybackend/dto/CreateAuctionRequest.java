package com.swaply.swaplybackend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CreateAuctionRequest {
    private Long listingId;
    private BigDecimal startingPrice;
    private BigDecimal minIncrement;
    private LocalDateTime endTime;

    public Long getListingId() { return listingId; }
    public void setListingId(Long listingId) { this.listingId = listingId; }

    public BigDecimal getStartingPrice() { return startingPrice; }
    public void setStartingPrice(BigDecimal startingPrice) { this.startingPrice = startingPrice; }

    public BigDecimal getMinIncrement() { return minIncrement; }
    public void setMinIncrement(BigDecimal minIncrement) { this.minIncrement = minIncrement; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
}

