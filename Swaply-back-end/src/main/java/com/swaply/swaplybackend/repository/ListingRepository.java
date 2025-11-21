package com.swaply.swaplybackend.repository;

import com.swaply.swaplybackend.entity.Listing;
import com.swaply.swaplybackend.entity.User;
import com.swaply.swaplybackend.enums.Category;
import com.swaply.swaplybackend.enums.ListingCondition;
import com.swaply.swaplybackend.enums.ListingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ListingRepository extends JpaRepository<Listing, Long> {

    //query methods
    List<Listing> findByUserUserId(Long userId);
    List<Listing> findByStatus(ListingStatus status);
    List<Listing> findByCategory(Category category);
    List<Listing> findByCondition(ListingCondition condition);

    //find by title containing keyword (case insensitive)
    List<Listing> findByTitleContainingIgnoreCase(String title);

    //find by price range
    List<Listing> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);

    //filter by status + criteria
    List<Listing> findByStatusAndCategory(ListingStatus status, Category category);

    //filter by status + date crated
    List<Listing> findByStatusOrderByCreatedDateDesc(ListingStatus status);

    //custom query keyword search in title or description
    @Query("SELECT l FROM Listing l WHERE " + "(LOWER(l.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(l.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " + "l.status = :status")
    List<Listing> searchByKeywordAndStatus(@Param("keyword") String keyword, @Param("status") ListingStatus status);


    Long countByUserUserId(Long userId);

    //check if good exists by status
    boolean existsByListingIdAndStatus(Long listingId, ListingStatus status);

    //count listings by created date range
    long countByCreatedDateBetween(LocalDateTime start, LocalDateTime end);
}
