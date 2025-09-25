package com.swaply.swaplybackend.dto;

import com.swaply.swaplybackend.enums.Category;
import com.swaply.swaplybackend.enums.ListingCondition;
import com.swaply.swaplybackend.enums.ListingStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ListingDto {
    private Long listingId;  // Changed from long to Long for consistency
    private String title;
    private String description;
    private BigDecimal price;
    private Category category;
    private ListingCondition condition;
    private String imageUrl;
    private ListingStatus status;
    private Long userId;
    private String userName;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    public ListingDto(){}

    public ListingDto(Long listingId, String title, String description, BigDecimal price, Category category, ListingCondition condition, String imageUrl, ListingStatus status, Long userId, String userName, LocalDateTime createdDate, LocalDateTime updatedDate) {
        this.listingId = listingId;
        this.title = title;
        this.description = description;
        this.price = price;
        this.category = category;
        this.condition = condition;
        this.imageUrl = imageUrl;
        this.status = status;
        this.userId = userId;
        this.userName = userName;
        this.createdDate = createdDate;
        this.updatedDate = updatedDate;
    }

    // Getters and Setters
    public Long getListingId() {
        return listingId;
    }

    public void setListingId(Long listingId) {
        this.listingId = listingId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public ListingCondition getCondition() {
        return condition;
    }

    public void setCondition(ListingCondition condition) {
        this.condition = condition;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }


    public ListingStatus getStatus() {
        return status;
    }

    public void setStatus(ListingStatus status) {
        this.status = status;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDateTime getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(LocalDateTime updatedDate) {
        this.updatedDate = updatedDate;
    }

}
