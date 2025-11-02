package com.swaply.swaplybackend.controller;

import com.swaply.swaplybackend.dto.AuctionDto;
import com.swaply.swaplybackend.dto.CreateAuctionRequest;
import com.swaply.swaplybackend.dto.PlaceBidRequest;
import com.swaply.swaplybackend.service.IAuctionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auctions")
public class AuctionController {

    private final IAuctionService auctionService;

    public AuctionController(IAuctionService auctionService) {
        this.auctionService = auctionService;
    }

    @PostMapping("/user/{userId}")
    public ResponseEntity<?> createAuction(@PathVariable Long userId, @RequestBody CreateAuctionRequest request) {
        try {
            AuctionDto dto = auctionService.createAuction(userId, request);
            return new ResponseEntity<>(dto, HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/active")
    public List<AuctionDto> activeAuctions() {
        return auctionService.getActiveAuctions();
    }

    @GetMapping("/{auctionId}")
    public ResponseEntity<?> get(@PathVariable Long auctionId) {
        try {
            return ResponseEntity.ok(auctionService.getAuction(auctionId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PostMapping("/{auctionId}/bid/user/{userId}")
    public ResponseEntity<?> bid(@PathVariable Long auctionId, @PathVariable Long userId, @RequestBody PlaceBidRequest req) {
        try {
            AuctionDto dto = auctionService.placeBid(auctionId, userId, req.getAmount());
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}


