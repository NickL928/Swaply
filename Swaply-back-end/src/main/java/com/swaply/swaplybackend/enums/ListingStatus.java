package com.swaply.swaplybackend.enums;

public enum ListingStatus {
    ACTIVE("Active"),
    INACTIVE("Inactive"),
    PENDING("Pending"),
    EXPIRED("Expired"),
    SOLD("Sold"),
    DELETED("Deleted");

    private final String displayName;

    ListingStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
