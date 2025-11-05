package com.swaply.swaplybackend.service;

import com.swaply.swaplybackend.entity.Auction;
import com.swaply.swaplybackend.enums.AuctionStatus;
import com.swaply.swaplybackend.repository.AuctionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class AuctionCleanupScheduler {

    private static final Logger log = LoggerFactory.getLogger(AuctionCleanupScheduler.class);

    private final AuctionRepository auctionRepository;

    public AuctionCleanupScheduler(AuctionRepository auctionRepository) {
        this.auctionRepository = auctionRepository;
    }

    // Run every 30 seconds to close expired auctions
    @Scheduled(fixedDelay = 30000)
    @Transactional
    public void closeExpiredAuctions() {
        LocalDateTime now = LocalDateTime.now();
        List<Auction> expired = auctionRepository.findByStatusAndEndTimeBefore(AuctionStatus.ACTIVE, now);
        if (expired.isEmpty()) return;
        for (Auction a : expired) {
            a.setStatus(AuctionStatus.ENDED);
        }
        auctionRepository.saveAll(expired);
        log.info("Closed {} expired auctions", expired.size());
    }
}

