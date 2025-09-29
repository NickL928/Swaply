package com.swaply.swaplybackend.dto;

import com.swaply.swaplybackend.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class UpdateOrderDto {

    @NotNull(message = "Order status cannot be null")
    private OrderStatus status;

    @Size(max = 500, message = "Note cannot exceed 500 characters")
    private String notes;

    public UpdateOrderDto() {}

    public UpdateOrderDto(OrderStatus status, String notes) {
        this.status = status;
        this.notes = notes;
    }

    // Getters and Setters

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
