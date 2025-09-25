package com.swaply.swaplybackend.service;

import com.swaply.swaplybackend.dto.CreateListingDto;
import com.swaply.swaplybackend.dto.ListingDto;
import com.swaply.swaplybackend.dto.UpdateListingDto;
import com.swaply.swaplybackend.enums.Category;

import java.util.List;

public interface IListingService {

    ListingDto createListing(CreateListingDto createListingDto, Long userId);

    ListingDto getListingById(Long id);

    List<ListingDto> getAllActiveListings();

    List<ListingDto> getListingsByCategory(Category category);

    List<ListingDto> getListingsByUserId(Long userId);

    List<ListingDto> searchListings(String keyword);

    ListingDto updateListing(Long listingId, UpdateListingDto updateListingDto);

    void deleteListing(Long listingId, Long userId);

    void markAsSold(Long listingId, Long userId);

}
