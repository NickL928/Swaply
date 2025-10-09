package com.swaply.swaplybackend.service;

import com.swaply.swaplybackend.dto.CartItemResponse;
import com.swaply.swaplybackend.entity.CartItem;
import com.swaply.swaplybackend.entity.Listing;
import com.swaply.swaplybackend.entity.User;
import com.swaply.swaplybackend.enums.ListingStatus;
import com.swaply.swaplybackend.repository.CartItemRepository;
import com.swaply.swaplybackend.repository.ListingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ListingRepository listingRepository;

    public CartService(CartItemRepository cartItemRepository, ListingRepository listingRepository) {
        this.cartItemRepository = cartItemRepository;
        this.listingRepository = listingRepository;
    }

    @Transactional
    public CartItemResponse addItem(User user, Long listingId, int quantity) {
        int effectiveQty = (quantity <= 0) ? 1 : quantity; // ensure effectively final for lambdas
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new IllegalArgumentException("Listing not found"));
        if (listing.getUser().getUserId().equals(user.getUserId())) {
            throw new IllegalArgumentException("Cannot add your own listing to cart");
        }
        if (listing.getStatus() != ListingStatus.ACTIVE) {
            throw new IllegalArgumentException("Listing is not active");
        }
        CartItem cartItem = cartItemRepository.findByUserAndListing(user, listing)
                .map(ci -> { ci.setQuantity(ci.getQuantity() + effectiveQty); return ci; })
                .orElseGet(() -> {
                    CartItem ci = new CartItem();
                    ci.setUser(user);
                    ci.setListing(listing);
                    ci.setQuantity(effectiveQty);
                    return ci;
                });
        CartItem saved = cartItemRepository.save(cartItem);
        return toResponse(saved);
    }

    public List<CartItemResponse> list(User user) {
        return cartItemRepository.findByUser(user).stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public void remove(User user, Long cartItemId) {
        CartItem ci = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found"));
        if (!ci.getUser().getUserId().equals(user.getUserId())) {
            throw new IllegalArgumentException("Forbidden");
        }
        cartItemRepository.delete(ci);
    }

    @Transactional
    public void clear(User user) {
        cartItemRepository.deleteByUser(user);
    }

    private CartItemResponse toResponse(CartItem ci) {
        CartItemResponse resp = new CartItemResponse();
        resp.setCartItemId(ci.getId());
        resp.setListingId(ci.getListing().getListingId());
        resp.setTitle(ci.getListing().getTitle());
        BigDecimal price = ci.getListing().getPrice();
        resp.setPrice(price);
        resp.setQuantity(ci.getQuantity());
        resp.setLineTotal(price.multiply(BigDecimal.valueOf(ci.getQuantity())));
        resp.setImageUrl(ci.getListing().getImageUrl());
        return resp;
    }
}
