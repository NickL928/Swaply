package com.swaply.swaplybackend.repository;

import com.swaply.swaplybackend.entity.Auction;
import com.swaply.swaplybackend.enums.AuctionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AuctionRepository extends JpaRepository<Auction, Long> {
    List<Auction> findByStatusOrderByCreatedDateDesc(AuctionStatus status);
    Optional<Auction> findByListing_ListingId(Long listingId);
}

