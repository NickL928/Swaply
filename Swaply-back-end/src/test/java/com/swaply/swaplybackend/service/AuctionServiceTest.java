package com.swaply.swaplybackend.service;

import com.swaply.swaplybackend.dto.AuctionDto;
import com.swaply.swaplybackend.dto.CreateAuctionRequest;
import com.swaply.swaplybackend.entity.Auction;
import com.swaply.swaplybackend.entity.Bid;
import com.swaply.swaplybackend.entity.Listing;
import com.swaply.swaplybackend.entity.User;
import com.swaply.swaplybackend.enums.AuctionStatus;
import com.swaply.swaplybackend.enums.ListingStatus;
import com.swaply.swaplybackend.repository.AuctionRepository;
import com.swaply.swaplybackend.repository.BidRepository;
import com.swaply.swaplybackend.repository.ListingRepository;
import com.swaply.swaplybackend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuctionServiceTest {

    @Mock AuctionRepository auctionRepository;
    @Mock BidRepository bidRepository;
    @Mock ListingRepository listingRepository;
    @Mock UserRepository userRepository;

    @InjectMocks AuctionService auctionService;

    private Listing listing;
    private Auction auction;

    @BeforeEach
    void setup() {
        User seller = new User();
        seller.setUserId(7L);

        listing = new Listing();
        listing.setListingId(11L);
        listing.setStatus(ListingStatus.ACTIVE);
        listing.setUser(seller);
        listing.setTitle("Rare sneakers");

        auction = new Auction();
        auction.setAuctionId(99L);
        auction.setListing(listing);
        auction.setStatus(AuctionStatus.ACTIVE);
        auction.setCurrentPrice(new BigDecimal("100"));
        auction.setStartingPrice(new BigDecimal("100"));
        auction.setMinIncrement(new BigDecimal("5"));
        auction.setEndTime(LocalDateTime.now().plusHours(2));
    }

    @Test
    void placeBid_updatesCurrentPrice_whenValid() {
        User bidder = new User();
        bidder.setUserId(55L);
        bidder.setUserName("buyer");

        when(auctionRepository.findById(99L)).thenReturn(Optional.of(auction));
        when(userRepository.findById(55L)).thenReturn(Optional.of(bidder));
        when(auctionRepository.save(auction)).thenReturn(auction);

        AuctionDto dto = auctionService.placeBid(99L, 55L, new BigDecimal("120"));

        assertThat(dto.getCurrentPrice()).isEqualByComparingTo("120");
        verify(bidRepository).save(any(Bid.class));
        verify(auctionRepository, atLeastOnce()).save(auction);
    }

    @Test
    void placeBid_rejectsOwner() {
        when(auctionRepository.findById(99L)).thenReturn(Optional.of(auction));
        when(userRepository.findById(7L)).thenReturn(Optional.of(listing.getUser()));

        assertThatThrownBy(() -> auctionService.placeBid(99L, 7L, new BigDecimal("110")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Owner cannot bid");
        verify(bidRepository, never()).save(any());
    }

    @Test
    void placeBid_requiresMinimumIncrement() {
        User bidder = new User();
        bidder.setUserId(55L);
        when(auctionRepository.findById(99L)).thenReturn(Optional.of(auction));
        when(userRepository.findById(55L)).thenReturn(Optional.of(bidder));

        assertThatThrownBy(() -> auctionService.placeBid(99L, 55L, new BigDecimal("102")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Bid must be at least");
    }

    @Test
    void createAuction_replacesEndedOneAndValidatesOwnership() {
        CreateAuctionRequest request = new CreateAuctionRequest();
        request.setListingId(11L);
        request.setStartingPrice(new BigDecimal("100"));
        request.setMinIncrement(new BigDecimal("5"));
        request.setEndTime(LocalDateTime.now().plusHours(1));

        Auction existing = new Auction();
        existing.setAuctionId(77L);
        existing.setListing(listing);
        existing.setStatus(AuctionStatus.ENDED);

        when(listingRepository.findById(11L)).thenReturn(Optional.of(listing));
        when(auctionRepository.findByListing_ListingId(11L)).thenReturn(Optional.of(existing));
        when(auctionRepository.save(any(Auction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuctionDto dto = auctionService.createAuction(7L, request);

        assertThat(dto.getListingId()).isEqualTo(11L);
        verify(bidRepository).deleteByAuction(existing);
        verify(auctionRepository).delete(existing);
    }
}

