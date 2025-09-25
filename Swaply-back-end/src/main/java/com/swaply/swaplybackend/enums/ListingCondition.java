package com.swaply.swaplybackend.enums;

public enum ListingCondition {
    NEW("New"),
    LIKE_NEW("Like New"),
    GOOD("Good"),
    FAIR("Fair"),
    POOR("Poor");

    private final String displayName;

    ListingCondition(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
