package com.swaply.swaplybackend.dto;

import java.math.BigDecimal;

public class PlaceBidRequest {
    private BigDecimal amount;

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}

