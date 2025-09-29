package com.swaply.swaplybackend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateOrderDto {

    @NotNull(message = "Buyer User ID cannot be null")
    private Long buyerId;

    @NotNull(message = "Listing ID cannot be null")
    private Long listingId;

    @Size(max = 500, message = "Note cannot exceed 500 characters")
    private String notes;

    public CreateOrderDto() {}

    public CreateOrderDto(Long buyerId, Long listingId, String notes) {
        this.buyerId = buyerId;
        this.listingId = listingId;
        this.notes = notes;
    }

    // Getters and Setters
    public Long getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(Long buyerId) {
        this.buyerId = buyerId;
    }

    public Long getListingId() {
        return listingId;
    }

    public void setListingId(Long listingId) {
        this.listingId = listingId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}

