package com.swaply.swaplybackend.cache.listing;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

public class BPlusTreeIndex implements ListingIndex {

    private final ConcurrentHashMap<Long, ListingCacheEntry> entriesById = new ConcurrentHashMap<>();
    private final ConcurrentSkipListMap<BigDecimal, List<ListingCacheEntry>> priceTree = new ConcurrentSkipListMap<>();
    private final ConcurrentSkipListMap<LocalDateTime, List<ListingCacheEntry>> createdTree = new ConcurrentSkipListMap<>();
    private final ConcurrentSkipListMap<Long, List<ListingCacheEntry>> popularityTree = new ConcurrentSkipListMap<>(Comparator.reverseOrder());

    @Override
    public synchronized void insert(ListingCacheEntry entry) {
        entriesById.put(entry.getListingId(), entry);
        priceTree.computeIfAbsent(entry.getPrice(), k -> new ArrayList<>()).add(entry);
        createdTree.computeIfAbsent(entry.getCreatedDate(), k -> new ArrayList<>()).add(entry);
        popularityTree.computeIfAbsent(entry.getPopularityScore(), k -> new ArrayList<>()).add(entry);
    }

    @Override
    public synchronized void remove(Long listingId) {
        ListingCacheEntry removed = entriesById.remove(listingId);
        if (removed == null) {
            return;
        }
        removeFromBucket(priceTree, removed.getPrice(), removed);
        removeFromBucket(createdTree, removed.getCreatedDate(), removed);
        removeFromBucket(popularityTree, removed.getPopularityScore(), removed);
    }

    @Override
    public synchronized void rebuild(List<ListingCacheEntry> entries) {
        entriesById.clear();
        priceTree.clear();
        createdTree.clear();
        popularityTree.clear();
        entries.forEach(this::insert);
    }

    @Override
    public synchronized List<ListingCacheEntry> fetchLatest(int limit) {
        return slice(createdTree.descendingMap().values(), limit);
    }

    @Override
    public synchronized List<ListingCacheEntry> fetchPopular(int limit) {
        return slice(popularityTree.values(), limit);
    }

    @Override
    public synchronized List<ListingCacheEntry> fetchByPriceRange(BigDecimal min, BigDecimal max, int limit) {
        return slice(priceTree.subMap(min, true, max, true).values(), limit);
    }

    private List<ListingCacheEntry> slice(Iterable<List<ListingCacheEntry>> buckets, int limit) {
        List<ListingCacheEntry> result = new ArrayList<>(Math.min(limit, 64));
        for (List<ListingCacheEntry> bucket : buckets) {
            for (ListingCacheEntry entry : bucket) {
                result.add(entry);
                if (result.size() >= limit) {
                    return result;
                }
            }
        }
        return result;
    }

    private void removeFromBucket(ConcurrentSkipListMap<?, List<ListingCacheEntry>> tree, Object key, ListingCacheEntry entry) {
        List<ListingCacheEntry> bucket = tree.get(key);
        if (bucket == null) {
            return;
        }
        bucket.remove(entry);
        if (bucket.isEmpty()) {
            tree.remove(key);
        }
    }
}
