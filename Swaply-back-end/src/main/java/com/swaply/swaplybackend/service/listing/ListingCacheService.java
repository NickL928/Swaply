package com.swaply.swaplybackend.service.listing;

import com.swaply.swaplybackend.cache.listing.BPlusTreeIndex;
import com.swaply.swaplybackend.cache.listing.ListingCacheEntry;
import com.swaply.swaplybackend.cache.listing.ListingIndex;
import com.swaply.swaplybackend.config.ListingCacheProperties;
import com.swaply.swaplybackend.dto.ListingDto;
import com.swaply.swaplybackend.entity.Listing;
import com.swaply.swaplybackend.enums.ListingStatus;
import com.swaply.swaplybackend.repository.ListingRepository;
import com.swaply.swaplybackend.service.ListingMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class ListingCacheService {

    private static final Logger log = LoggerFactory.getLogger(ListingCacheService.class);

    private final ListingRepository listingRepository;
    private final ListingCacheProperties properties;
    private final ListingIndex index = new BPlusTreeIndex();
    private final Map<Long, ListingCacheEntry> entriesById = new ConcurrentHashMap<>();

    public ListingCacheService(ListingRepository listingRepository, ListingCacheProperties properties) {
        this.listingRepository = listingRepository;
        this.properties = properties;
    }

    @PostConstruct
    public void init() {
        if (!properties.isEnabled()) {
            log.info("Listing cache disabled via configuration");
            return;
        }
        rebuildCache("startup");
    }

    @Scheduled(fixedDelayString = "${listing.cache.refresh-interval-ms:60000}")
    public void scheduledRefresh() {
        if (properties.isEnabled()) {
            rebuildCache("scheduled");
        }
    }

    @Transactional(readOnly = true)
    public void rebuildCache(String reason) {
        List<Listing> listings = listingRepository.findByStatusOrderByCreatedDateDesc(ListingStatus.ACTIVE);
        List<ListingCacheEntry> entries = listings.stream()
                .limit(properties.getPreloadLimit())
                .map(ListingMapper::toDto)
                .map(dto -> new ListingCacheEntry(dto, popularityScore(dto)))
                .collect(Collectors.toList());
        entriesById.clear();
        entries.forEach(entry -> entriesById.put(entry.getListingId(), entry));
        index.rebuild(entries);
        log.info("Listing cache rebuilt ({} entries) due to {}", entries.size(), reason);
    }

    public List<ListingDto> latest(int limit) {
        return toDtoList(index.fetchLatest(limit));
    }

    public List<ListingDto> popular(int limit) {
        return toDtoList(index.fetchPopular(limit));
    }

    public List<ListingDto> priceRange(BigDecimal min, BigDecimal max, int limit) {
        return toDtoList(index.fetchByPriceRange(min, max, limit));
    }

    public Optional<ListingDto> findById(Long id) {
        ListingCacheEntry entry = entriesById.get(id);
        return entry != null ? Optional.of(entry.getListingDto()) : Optional.empty();
    }

    public void upsert(Listing listing) {
        ListingDto dto = ListingMapper.toDto(listing);
        ListingCacheEntry entry = new ListingCacheEntry(dto, popularityScore(dto));
        entriesById.put(entry.getListingId(), entry);
        index.insert(entry);
    }

    public void evict(Long listingId) {
        entriesById.remove(listingId);
        index.remove(listingId);
    }

    private List<ListingDto> toDtoList(List<ListingCacheEntry> entries) {
        return entries.stream().map(ListingCacheEntry::getListingDto).collect(Collectors.toList());
    }

    private long popularityScore(ListingDto dto) {
        LocalDateTime created = dto.getCreatedDate() != null ? dto.getCreatedDate() : dto.getUpdatedDate();
        if (created == null) {
            created = LocalDateTime.now();
        }
        long minutesAgo = Duration.between(created, LocalDateTime.now()).toMinutes();
        long recencyScore = Math.max(0, 1_000_000L - minutesAgo);
        BigDecimal price = dto.getPrice() != null ? dto.getPrice() : BigDecimal.ZERO;
        return recencyScore + price.longValue();
    }
}

