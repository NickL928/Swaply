package com.swaply.swaplybackend.service;

import com.swaply.swaplybackend.dto.AuctionDto;
import com.swaply.swaplybackend.dto.CreateAuctionRequest;

import java.math.BigDecimal;
import java.util.List;

public interface IAuctionService {
    AuctionDto createAuction(Long userId, CreateAuctionRequest request);
    List<AuctionDto> getActiveAuctions();
    AuctionDto getAuction(Long auctionId);
    AuctionDto placeBid(Long auctionId, Long userId, BigDecimal amount);
}

