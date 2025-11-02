package com.swaply.swaplybackend.repository;

import com.swaply.swaplybackend.entity.Auction;
import com.swaply.swaplybackend.entity.Bid;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BidRepository extends JpaRepository<Bid, Long> {
    List<Bid> findByAuctionOrderByAmountDescCreatedDateDesc(Auction auction);
    Optional<Bid> findTopByAuctionOrderByAmountDescCreatedDateDesc(Auction auction);
}

