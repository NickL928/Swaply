package com.swaply.swaplybackend.service;

import com.swaply.swaplybackend.dto.AuctionDto;
import com.swaply.swaplybackend.dto.CreateAuctionRequest;
import com.swaply.swaplybackend.entity.Auction;
import com.swaply.swaplybackend.entity.Bid;
import com.swaply.swaplybackend.entity.Listing;
import com.swaply.swaplybackend.entity.User;
import com.swaply.swaplybackend.enums.AuctionStatus;
import com.swaply.swaplybackend.enums.ListingStatus;
import com.swaply.swaplybackend.exception.InvalidListingException;
import com.swaply.swaplybackend.exception.ListingNotFoundException;
import com.swaply.swaplybackend.repository.AuctionRepository;
import com.swaply.swaplybackend.repository.BidRepository;
import com.swaply.swaplybackend.repository.ListingRepository;
import com.swaply.swaplybackend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AuctionService implements IAuctionService {

    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;
    private final ListingRepository listingRepository;
    private final UserRepository userRepository;

    public AuctionService(AuctionRepository auctionRepository, BidRepository bidRepository,
                          ListingRepository listingRepository, UserRepository userRepository) {
        this.auctionRepository = auctionRepository;
        this.bidRepository = bidRepository;
        this.listingRepository = listingRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public AuctionDto createAuction(Long userId, CreateAuctionRequest request) {
        Listing listing = listingRepository.findById(request.getListingId())
                .orElseThrow(() -> new ListingNotFoundException("Listing not found: " + request.getListingId()));
        if (!listing.getUser().getUserId().equals(userId)) {
            throw new InvalidListingException("You are not the owner of this listing");
        }
        if (listing.getStatus() != ListingStatus.ACTIVE) {
            throw new InvalidListingException("Listing must be ACTIVE to create auction");
        }

        // Check for existing auction
        Optional<Auction> existing = auctionRepository.findByListing_ListingId(listing.getListingId());
        if (existing.isPresent()) {
            Auction existingAuction = existing.get();
            if (existingAuction.getStatus() == AuctionStatus.ACTIVE) {
                throw new InvalidListingException("An active auction already exists for this listing");
            }
            bidRepository.deleteByAuction(existingAuction);
            auctionRepository.delete(existingAuction);
            auctionRepository.flush(); // Force deletion to database immediately
        }

        if (request.getStartingPrice() == null || request.getMinIncrement() == null || request.getEndTime() == null) {
            throw new InvalidListingException("startingPrice, minIncrement, endTime are required");
        }
        if (request.getEndTime().isBefore(LocalDateTime.now().plusMinutes(1))) {
            throw new InvalidListingException("End time must be at least 1 minute in the future");
        }

        Auction auction = new Auction();
        auction.setListing(listing);
        auction.setStatus(AuctionStatus.ACTIVE);
        auction.setStartingPrice(request.getStartingPrice());
        auction.setCurrentPrice(request.getStartingPrice());
        auction.setMinIncrement(request.getMinIncrement());
        auction.setEndTime(request.getEndTime());
        Auction saved = auctionRepository.save(auction);
        return toDto(saved);
    }

    @Override
    public List<AuctionDto> getActiveAuctions() {
        return auctionRepository.findByStatusOrderByCreatedDateDesc(AuctionStatus.ACTIVE)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public AuctionDto getAuction(Long auctionId) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new IllegalArgumentException("Auction not found: " + auctionId));
        return toDto(auction);
    }

    @Override
    @Transactional
    public AuctionDto placeBid(Long auctionId, Long userId, BigDecimal amount) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new IllegalArgumentException("Auction not found: " + auctionId));
        if (auction.getStatus() != AuctionStatus.ACTIVE) {
            throw new IllegalStateException("Auction is not active");
        }
        if (auction.getEndTime().isBefore(LocalDateTime.now())) {
            auction.setStatus(AuctionStatus.ENDED);
            auctionRepository.save(auction);
            throw new IllegalStateException("Auction has ended");
        }
        User bidder = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        if (auction.getListing().getUser().getUserId().equals(userId)) {
            throw new IllegalStateException("Owner cannot bid on own auction");
        }
        BigDecimal base = auction.getCurrentPrice();
        BigDecimal minRequired = base.add(auction.getMinIncrement());
        if (amount == null || amount.compareTo(minRequired) < 0) {
            throw new IllegalArgumentException("Bid must be at least " + minRequired);
        }

        Bid bid = new Bid();
        bid.setAuction(auction);
        bid.setBidder(bidder);
        bid.setAmount(amount);
        bidRepository.save(bid);

        auction.setCurrentPrice(amount);
        auction.setHighestBidder(bidder);
        Auction updated = auctionRepository.save(auction);
        return toDto(updated);
    }

    private AuctionDto toDto(Auction a) {
        AuctionDto dto = new AuctionDto();
        dto.setAuctionId(a.getAuctionId());
        dto.setListingId(a.getListing().getListingId());
        dto.setTitle(a.getListing().getTitle());
        dto.setImageUrl(a.getListing().getImageUrl());
        dto.setStartingPrice(a.getStartingPrice());
        dto.setCurrentPrice(a.getCurrentPrice());
        dto.setMinIncrement(a.getMinIncrement());
        dto.setStatus(a.getStatus());
        dto.setSellerId(a.getListing().getUser().getUserId());
        dto.setSellerUsername(a.getListing().getUser().getUserName());
        dto.setSellerProfileImageUrl(a.getListing().getUser().getProfileImageUrl());
        if (a.getHighestBidder() != null) {
            dto.setHighestBidderId(a.getHighestBidder().getUserId());
            dto.setHighestBidderUsername(a.getHighestBidder().getUserName());
        }
        dto.setEndTime(a.getEndTime());
        return dto;
    }
}
