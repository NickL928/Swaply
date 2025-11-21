package com.swaply.swaplybackend.cache.listing;

import java.math.BigDecimal;
import java.util.List;

public interface ListingIndex {
    void insert(ListingCacheEntry entry);

    void remove(Long listingId);

    void rebuild(List<ListingCacheEntry> entries);

    List<ListingCacheEntry> fetchLatest(int limit);

    List<ListingCacheEntry> fetchPopular(int limit);

    List<ListingCacheEntry> fetchByPriceRange(BigDecimal min, BigDecimal max, int limit);
}
