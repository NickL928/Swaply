package com.swaply.swaplybackend.service;

import com.swaply.swaplybackend.dto.CreateListingDto;
import com.swaply.swaplybackend.dto.ListingDto;
import com.swaply.swaplybackend.dto.UpdateListingDto;
import com.swaply.swaplybackend.entity.Listing;
import com.swaply.swaplybackend.entity.User;
import com.swaply.swaplybackend.enums.Category;
import com.swaply.swaplybackend.enums.ListingCondition;
import com.swaply.swaplybackend.enums.ListingStatus;
import com.swaply.swaplybackend.exception.InvalidListingException;
import com.swaply.swaplybackend.repository.ListingRepository;
import com.swaply.swaplybackend.repository.UserRepository;
import com.swaply.swaplybackend.service.listing.ListingCacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListingServiceTest {

    @Mock ListingRepository listingRepository;
    @Mock UserRepository userRepository;
    @Mock ListingCacheService listingCacheService;

    @InjectMocks ListingService listingService;

    private User seller;

    @BeforeEach
    void setup() {
        seller = new User();
        seller.setUserId(42L);
        seller.setUserName("nick");
    }

    @Test
    void createListing_persistsEntity_andCachesResult() {
        CreateListingDto dto = new CreateListingDto();
        dto.setTitle("Vintage Camera");
        dto.setDescription("Perfect shape");
        dto.setPrice(new BigDecimal("150"));
        dto.setCategory(Category.ELECTRONICS);
        dto.setCondition(ListingCondition.LIKE_NEW);
        dto.setImageUrl("https://example.com/cam.jpg");

        Listing saved = new Listing();
        saved.setListingId(100L);
        saved.setTitle(dto.getTitle());
        saved.setDescription(dto.getDescription());
        saved.setPrice(dto.getPrice());
        saved.setCategory(dto.getCategory());
        saved.setCondition(dto.getCondition());
        saved.setImageUrl(dto.getImageUrl());
        saved.setStatus(ListingStatus.ACTIVE);
        saved.setUser(seller);

        when(userRepository.findById(42L)).thenReturn(Optional.of(seller));
        when(listingRepository.save(any(Listing.class))).thenReturn(saved);

        ListingDto result = listingService.createListing(dto, 42L);

        assertThat(result.getListingId()).isEqualTo(100L);
        assertThat(result.getStatus()).isEqualTo(ListingStatus.ACTIVE);
        assertThat(result.getUserId()).isEqualTo(42L);

        verify(listingRepository).save(argThat(listing ->
                listing.getUser().equals(seller) &&
                listing.getStatus() == ListingStatus.ACTIVE &&
                listing.getTitle().equals("Vintage Camera")));
        verify(listingCacheService).upsert(saved);
    }

    @Test
    void createListing_unknownUser_throwsInvalidListingException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        CreateListingDto dto = new CreateListingDto();
        assertThatThrownBy(() -> listingService.createListing(dto, 99L))
                .isInstanceOf(InvalidListingException.class)
                .hasMessageContaining("User with ID 99");
    }

    @Test
    void updateListing_rejectsUnauthorizedUser() {
        Listing existing = new Listing();
        existing.setListingId(5L);
        existing.setUser(seller);
        when(listingRepository.findById(5L)).thenReturn(Optional.of(existing));

        UpdateListingDto update = new UpdateListingDto();
        update.setUserId(7L);

        assertThatThrownBy(() -> listingService.updateListing(5L, update))
                .isInstanceOf(InvalidListingException.class)
                .hasMessageContaining("authorized");
        verify(listingRepository, never()).save(any());
    }
}

