package com.swaply.swaplybackend.repository;

import com.swaply.swaplybackend.entity.CartItem;
import com.swaply.swaplybackend.entity.User;
import com.swaply.swaplybackend.entity.Listing;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUser(User user);
    Optional<CartItem> findByUserAndListing(User user, Listing listing);
    void deleteByUser(User user);
}
