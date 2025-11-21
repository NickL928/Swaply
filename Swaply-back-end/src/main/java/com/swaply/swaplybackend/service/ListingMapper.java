package com.swaply.swaplybackend.service;

import com.swaply.swaplybackend.dto.ListingDto;
import com.swaply.swaplybackend.entity.Listing;

public final class ListingMapper {

    private ListingMapper() {
    }

    public static ListingDto toDto(Listing listing) {
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
        dto.setSellerUsername(listing.getUser().getUserName());
        dto.setSellerProfileImageUrl(listing.getUser().getProfileImageUrl());
        dto.setCreatedDate(listing.getCreatedDate());
        dto.setUpdatedDate(listing.getUpdatedDate());
        return dto;
    }
}

