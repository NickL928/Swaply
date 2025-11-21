package com.swaply.swaplybackend.service;

import com.swaply.swaplybackend.dto.CartItemResponse;
import com.swaply.swaplybackend.entity.CartItem;
import com.swaply.swaplybackend.entity.Listing;
import com.swaply.swaplybackend.entity.User;
import com.swaply.swaplybackend.enums.ListingStatus;
import com.swaply.swaplybackend.repository.CartItemRepository;
import com.swaply.swaplybackend.repository.ListingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock CartItemRepository cartItemRepository;
    @Mock ListingRepository listingRepository;

    @InjectMocks CartService cartService;

    private User buyer;
    private User seller;
    private Listing listing;

    @BeforeEach
    void setup() {
        seller = new User();
        seller.setUserId(1L);
        seller.setUserName("nick");

        buyer = new User();
        buyer.setUserId(2L);
        buyer.setUserName("buyer");

        listing = new Listing();
        listing.setListingId(10L);
        listing.setTitle("Vintage Camera");
        listing.setPrice(new BigDecimal("150"));
        listing.setStatus(ListingStatus.ACTIVE);
        listing.setUser(seller);
    }

    @Test
    void addItem_withNewListing_createsCartItem() {
        CartItem savedItem = new CartItem();
        savedItem.setId(1L);
        savedItem.setUser(buyer);
        savedItem.setListing(listing);
        savedItem.setQuantity(1);

        when(listingRepository.findById(10L)).thenReturn(Optional.of(listing));
        when(cartItemRepository.findByUserAndListing(buyer, listing)).thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(savedItem);

        CartItemResponse result = cartService.addItem(buyer, 10L, 1);

        assertThat(result.getCartItemId()).isEqualTo(1L);
        assertThat(result.getListingId()).isEqualTo(10L);
        assertThat(result.getQuantity()).isEqualTo(1);
        verify(cartItemRepository).save(argThat(item ->
                item.getUser().equals(buyer) &&
                item.getListing().equals(listing) &&
                item.getQuantity() == 1));
    }

    @Test
    void addItem_withExistingListing_incrementsQuantity() {
        CartItem existing = new CartItem();
        existing.setId(1L);
        existing.setUser(buyer);
        existing.setListing(listing);
        existing.setQuantity(2);

        when(listingRepository.findById(10L)).thenReturn(Optional.of(listing));
        when(cartItemRepository.findByUserAndListing(buyer, listing)).thenReturn(Optional.of(existing));
        when(cartItemRepository.save(existing)).thenReturn(existing);

        CartItemResponse result = cartService.addItem(buyer, 10L, 1);

        assertThat(result.getQuantity()).isEqualTo(3);
        verify(cartItemRepository).save(argThat(item -> item.getQuantity() == 3));
    }

    @Test
    void addItem_whenListingInactive_throwsException() {
        listing.setStatus(ListingStatus.SOLD);

        when(listingRepository.findById(10L)).thenReturn(Optional.of(listing));

        assertThatThrownBy(() -> cartService.addItem(buyer, 10L, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not active");
    }

    @Test
    void addItem_whenOwnListing_throwsException() {
        when(listingRepository.findById(10L)).thenReturn(Optional.of(listing));

        assertThatThrownBy(() -> cartService.addItem(seller, 10L, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("own listing");
    }

    @Test
    void list_returnsAllCartItems() {
        CartItem item1 = new CartItem();
        item1.setId(1L);
        item1.setUser(buyer);
        item1.setListing(listing);
        item1.setQuantity(2);

        when(cartItemRepository.findByUser(buyer)).thenReturn(List.of(item1));

        List<CartItemResponse> result = cartService.list(buyer);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getQuantity()).isEqualTo(2);
    }

    @Test
    void remove_deletesCartItem() {
        CartItem item = new CartItem();
        item.setId(5L);
        item.setUser(buyer);

        when(cartItemRepository.findById(5L)).thenReturn(Optional.of(item));

        cartService.remove(buyer, 5L);

        verify(cartItemRepository).delete(item);
    }

    @Test
    void remove_whenNotOwner_throwsException() {
        CartItem item = new CartItem();
        item.setId(5L);
        item.setUser(seller);

        when(cartItemRepository.findById(5L)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> cartService.remove(buyer, 5L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Forbidden");
    }

    @Test
    void clear_deletesAllUserCartItems() {
        cartService.clear(buyer);
        verify(cartItemRepository).deleteByUser(buyer);
    }
}

