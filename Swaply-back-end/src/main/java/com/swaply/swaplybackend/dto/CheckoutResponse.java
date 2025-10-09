package com.swaply.swaplybackend.dto;

import java.math.BigDecimal;
import java.util.List;

public class CheckoutResponse {
    private List<Long> orderIds;
    private int ordersCreated;
    private BigDecimal grandTotal;
    private int itemsCount;

    public List<Long> getOrderIds() { return orderIds; }
    public void setOrderIds(List<Long> orderIds) { this.orderIds = orderIds; }
    public int getOrdersCreated() { return ordersCreated; }
    public void setOrdersCreated(int ordersCreated) { this.ordersCreated = ordersCreated; }
    public BigDecimal getGrandTotal() { return grandTotal; }
    public void setGrandTotal(BigDecimal grandTotal) { this.grandTotal = grandTotal; }
    public int getItemsCount() { return itemsCount; }
    public void setItemsCount(int itemsCount) { this.itemsCount = itemsCount; }
}
