package com.swaply.swaplybackend.service;

import com.swaply.swaplybackend.dto.CreateListingDto;
import com.swaply.swaplybackend.dto.ListingDto;
import com.swaply.swaplybackend.dto.UpdateListingDto;
import com.swaply.swaplybackend.entity.Listing;
import com.swaply.swaplybackend.entity.User;
import com.swaply.swaplybackend.enums.Category;
import com.swaply.swaplybackend.enums.ListingStatus;
import com.swaply.swaplybackend.exception.InvalidListingException;
import com.swaply.swaplybackend.exception.ListingNotFoundException;
import com.swaply.swaplybackend.repository.ListingRepository;
import com.swaply.swaplybackend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ListingService implements IListingService {

    private final ListingRepository listingRepository;
    private final UserRepository userRepository;

    public ListingService(ListingRepository listingRepository, UserRepository userRepository) {
        this.listingRepository = listingRepository;
        this.userRepository = userRepository;
    }

    @Override
    public ListingDto createListing(CreateListingDto createListingDto, Long userId) {
        // 使用路径参数中的userId，而不是DTO中的userId
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidListingException("User with ID " + userId + " does not exist."));

        // Create new listing entity
        Listing listing = new Listing();
        listing.setTitle(createListingDto.getTitle());
        listing.setDescription(createListingDto.getDescription());
        listing.setPrice(createListingDto.getPrice());
        listing.setCategory(createListingDto.getCategory());
        listing.setCondition(createListingDto.getCondition());
        listing.setImageUrl(createListingDto.getImageUrl());
        listing.setUser(user);
        listing.setStatus(ListingStatus.ACTIVE);
        listing.setCreatedDate(LocalDateTime.now());
        listing.setUpdatedDate(LocalDateTime.now());

        Listing savedListing = listingRepository.save(listing);
        return convertToDto(savedListing);
    }


    @Override
    public ListingDto getListingById(Long id) {
        Listing listing = listingRepository.findById(id)
                .orElseThrow(() -> new ListingNotFoundException("Listing with ID " + id + " not found."));
        return convertToDto(listing);
    }


    @Override
    public List<ListingDto> getAllActiveListings() {
        List<Listing> activeListings = listingRepository.findByStatusOrderByCreatedDateDesc(ListingStatus.ACTIVE);
        return activeListings.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ListingDto> getListingsByCategory(Category category) {
        List<Listing> listings = listingRepository.findByStatusAndCategory(ListingStatus.ACTIVE, category);
        return listings.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ListingDto> getListingsByUserId(Long userId) {
        List<Listing> listings = listingRepository.findByUserId(userId);
        return listings.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ListingDto> searchListings(String keyword) {
        List<Listing> listings = listingRepository.searchByKeywordAndStatus(keyword, ListingStatus.ACTIVE);
        return listings.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public ListingDto updateListing(Long listingId, UpdateListingDto updateListingDto) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ListingNotFoundException("Listing not found with ID: " + listingId));

        //validate user authorization
        if (!listing.getUser().getUserId().equals(updateListingDto.getUserId())) {
            throw new InvalidListingException("User is not authorized to update this listing");
        }

        //update fields if they are provided
        if (updateListingDto.getTitle() != null) {
            listing.setTitle(updateListingDto.getTitle());
        }
        if (updateListingDto.getDescription() != null) {
            listing.setDescription(updateListingDto.getDescription());
        }
        if (updateListingDto.getPrice() != null) {
            listing.setPrice(updateListingDto.getPrice());
        }
        if (updateListingDto.getCategory() != null) {
            listing.setCategory(updateListingDto.getCategory());
        }
        if (updateListingDto.getCondition() != null) {
            listing.setCondition(updateListingDto.getCondition());
        }
        if (updateListingDto.getImageUrl() != null) {
            listing.setImageUrl(updateListingDto.getImageUrl());
        }
        if (updateListingDto.getStatus() != null) {
            listing.setStatus(updateListingDto.getStatus());
        }

        listing.setUpdatedDate(LocalDateTime.now());
        Listing updatedListing = listingRepository.save(listing);
        return convertToDto(updatedListing);
    }

    @Override
    public void deleteListing(Long listingId, Long userId) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ListingNotFoundException("Listing not found with ID: " + listingId));

        //validate user authorization
        if (!listing.getUser().getUserId().equals(userId)) {
            throw new InvalidListingException("User is not authorized to delete this listing");
        }

        //soft delete by setting status to DELETED
        listing.setStatus(ListingStatus.DELETED);
        listing.setUpdatedDate(LocalDateTime.now());
        listingRepository.save(listing);
    }

    @Override
    public void markAsSold(Long listingId, Long userId) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ListingNotFoundException("Listing not found with ID: " + listingId));

        if (!listing.getUser().getUserId().equals(userId)) {
            throw new InvalidListingException("User is not authorized to modify this listing");
        }

        if (listing.getStatus() != ListingStatus.ACTIVE) {
            throw new InvalidListingException("Only active listings can be marked as sold");
        }

        listing.setStatus(ListingStatus.SOLD);
        listing.setUpdatedDate(LocalDateTime.now());
        listingRepository.save(listing);
    }

    // entity to DTO conversion
    private ListingDto convertToDto(Listing listing) {
        ListingDto dto = new ListingDto();
        dto.setListingId(listing.getListingId());
        dto.setTitle(listing.getTitle());
        dto.setDescription(listing.getDescription());
        dto.setPrice(listing.getPrice());
        dto.setCategory(listing.getCategory());
        dto.setCondition(listing.getCondition());
        dto.setImageUrl(listing.getImageUrl());
        dto.setStatus(listing.getStatus());
        dto.setUserId(listing.getUser().getUserId());
        dto.setUserName(listing.getUser().getUserName());
        dto.setCreatedDate(listing.getCreatedDate());
        dto.setUpdatedDate(listing.getUpdatedDate());
        return dto;
    }

}
