package com.swaply.swaplybackend.cache.listing;

import com.swaply.swaplybackend.dto.ListingDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class ListingCacheEntry {

    private final Long listingId;
    private final BigDecimal price;
    private final LocalDateTime createdDate;
    private final long popularityScore;
    private final long createdEpoch;
    private final ListingDto listingDto;

    public ListingCacheEntry(ListingDto listingDto, long popularityScore) {
        this.listingId = listingDto.getListingId();
        this.price = listingDto.getPrice();
        LocalDateTime created = listingDto.getCreatedDate();
        if (created == null) {
            created = listingDto.getUpdatedDate();
        }
        this.createdDate = created != null ? created : LocalDateTime.now(ZoneOffset.UTC);
        this.popularityScore = popularityScore;
        this.createdEpoch = this.createdDate.toEpochSecond(ZoneOffset.UTC);
        this.listingDto = listingDto;
    }

    public Long getListingId() {
        return listingId;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public long getPopularityScore() {
        return popularityScore;
    }

    public long getCreatedEpoch() {
        return createdEpoch;
    }

    public ListingDto getListingDto() {
        return listingDto;
    }
}
