package com.swaply.swaplybackend.repository;

import com.swaply.swaplybackend.entity.Order;
import com.swaply.swaplybackend.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order,Long> {

    // Find orders by user ID
    List<Order> findByBuyerUserId(Long buyerId);

    // Find orders by seller id
    @Query("SELECT o FROM Order o WHERE o.listing.user.userId = :sellerId")
    List<Order> findBySellerUserId(@Param("sellerId") Long sellerId);

    // Find order by status
    List<Order> findByStatus(OrderStatus status);

    // Find by buyer amd status
    List<Order> findByBuyerUserIdAndStatus(Long buyerId, OrderStatus status);

    // Find by seller and status
    @Query("SELECT o FROM Order o WHERE o.listing.user.userId = :sellerId AND o.status = :status")
    List<Order> findBySellerUserIdAndStatus(@Param("sellerId") Long sellerId, @Param("status") OrderStatus status);

    // Find by listing id
    Optional<Order> findByListingListingId(Long listingId);

    // Check if user has ordered a specific listing
    Optional<Order> findByBuyerIdAndListingId(Long buyerId, Long listingId);
}
