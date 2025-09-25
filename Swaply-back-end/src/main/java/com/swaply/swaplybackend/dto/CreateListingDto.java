package com.swaply.swaplybackend.dto;

import com.swaply.swaplybackend.enums.Category;
import com.swaply.swaplybackend.enums.ListingCondition;

import java.math.BigDecimal;

public class CreateListingDto {
    private String title;
    private String description;
    private BigDecimal price;
    private Category category;
    private ListingCondition condition;
    private String imageUrl;
    private long userId;

    public CreateListingDto() {
    }

    public CreateListingDto(String title, String description, BigDecimal price, Category category, ListingCondition condition, String imageUrl, Long userId) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.category = category;
        this.condition = condition;
        this.imageUrl = imageUrl;
        this.userId = userId;
    }

    // Getters and Setters
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

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }
}
