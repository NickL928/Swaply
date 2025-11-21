package com.swaply.swaplybackend.service;

import com.swaply.swaplybackend.entity.Auction;
import com.swaply.swaplybackend.entity.Order;
import com.swaply.swaplybackend.entity.Listing;
import com.swaply.swaplybackend.enums.AuctionStatus;
import com.swaply.swaplybackend.enums.ListingStatus;
import com.swaply.swaplybackend.enums.OrderStatus;
import com.swaply.swaplybackend.repository.AuctionRepository;
import com.swaply.swaplybackend.repository.OrderRepository;
import com.swaply.swaplybackend.repository.ListingRepository;
import com.swaply.swaplybackend.service.listing.ListingCacheService;
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
    private final OrderRepository orderRepository;
    private final ListingRepository listingRepository;
    private final ListingCacheService listingCacheService;

    public AuctionCleanupScheduler(AuctionRepository auctionRepository,
                                   OrderRepository orderRepository,
                                   ListingRepository listingRepository,
                                   ListingCacheService listingCacheService) {
        this.auctionRepository = auctionRepository;
        this.orderRepository = orderRepository;
        this.listingRepository = listingRepository;
        this.listingCacheService = listingCacheService;
    }

    // Run every 30 seconds to close expired auctions
    @Scheduled(fixedDelay = 30000)
    @Transactional
    public void closeExpiredAuctions() {
        LocalDateTime now = LocalDateTime.now();
        List<Auction> expired = auctionRepository.findByStatusAndEndTimeBefore(AuctionStatus.ACTIVE, now);
        if (expired.isEmpty()) return;

        int closedCount = 0;
        int ordersCreated = 0;

        for (Auction auction : expired) {
            // Mark auction as ended
            auction.setStatus(AuctionStatus.ENDED);

            // If there's a winner, create an order and mark listing as sold
            if (auction.getHighestBidder() != null) {
                try {
                    // Create order for the winner
                    Order order = new Order();
                    order.setBuyer(auction.getHighestBidder());
                    order.setListing(auction.getListing());
                    order.setTotalAmount(auction.getCurrentPrice());
                    order.setQuantity(1);
                    order.setStatus(OrderStatus.PENDING);
                    order.setNotes("Won from auction #" + auction.getAuctionId());

                    orderRepository.save(order);
                    ordersCreated++;

                    // Mark listing as SOLD
                    Listing listing = auction.getListing();
                    listing.setStatus(ListingStatus.SOLD);
                    listingRepository.save(listing);

                    // Immediately remove from cache so it disappears from homepage
                    listingCacheService.evict(listing.getListingId());

                    log.info("Created order for auction #{} winner: {} ({})",
                             auction.getAuctionId(),
                             auction.getHighestBidder().getUserName(),
                             auction.getCurrentPrice());
                } catch (Exception e) {
                    log.error("Failed to create order for auction #{}: {}",
                             auction.getAuctionId(), e.getMessage());
                }
            } else {
                // No bids - auction ended without winner
                log.info("Auction #{} ended with no bids", auction.getAuctionId());
            }

            closedCount++;
        }

        auctionRepository.saveAll(expired);
        log.info("Closed {} expired auctions, created {} orders", closedCount, ordersCreated);
    }
}

