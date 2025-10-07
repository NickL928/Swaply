package com.swaply.swaplybackend.controller;

import com.swaply.swaplybackend.dto.CreateListingDto;
import com.swaply.swaplybackend.dto.ListingDto;
import com.swaply.swaplybackend.dto.UpdateListingDto;
import com.swaply.swaplybackend.enums.Category;
import com.swaply.swaplybackend.service.IListingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/listings")
public class ListingController {

    @Autowired
    private IListingService listingService;

    @PostMapping("/user/{userId}")
    public ResponseEntity<ListingDto> createListing(@RequestBody CreateListingDto createListingDto,
                                                   @PathVariable Long userId) {
        try {
            ListingDto createdListing = listingService.createListing(createListingDto, userId);
            return new ResponseEntity<>(createdListing, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ListingDto> getListingById(@PathVariable Long id) {
        try {
            ListingDto listing = listingService.getListingById(id);
            return new ResponseEntity<>(listing, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/active")
    public ResponseEntity<List<ListingDto>> getAllActiveListings() {
        try {
            List<ListingDto> listings = listingService.getAllActiveListings();
            return new ResponseEntity<>(listings, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<ListingDto>> getListingsByCategory(@PathVariable Category category) {
        try {
            List<ListingDto> listings = listingService.getListingsByCategory(category);
            return new ResponseEntity<>(listings, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ListingDto>> getListingsByUserId(@PathVariable Long userId) {
        try {
            List<ListingDto> listings = listingService.getListingsByUserId(userId);
            return new ResponseEntity<>(listings, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<ListingDto>> searchListings(@RequestParam String keyword) {
        try {
            List<ListingDto> listings = listingService.searchListings(keyword);
            return new ResponseEntity<>(listings, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{listingId}")
    public ResponseEntity<ListingDto> updateListing(@PathVariable Long listingId,
                                                   @RequestBody UpdateListingDto updateListingDto) {
        try {
            ListingDto updatedListing = listingService.updateListing(listingId, updateListingDto);
            return new ResponseEntity<>(updatedListing, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{listingId}/user/{userId}")
    public ResponseEntity<Void> deleteListing(@PathVariable Long listingId,
                                            @PathVariable Long userId) {
        try {
            listingService.deleteListing(listingId, userId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PatchMapping("/{listingId}/sold/user/{userId}")
    public ResponseEntity<Void> markAsSold(@PathVariable Long listingId,
                                         @PathVariable Long userId) {
        try {
            listingService.markAsSold(listingId, userId);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
