package com.swaply.swaplybackend.enums;

public enum Category {
    ELECTRONICS("Electronics"),
    BOOKS("Books"),
    FURNITURE("Furniture"),
    CLOTHING("Clothing"),
    SPORTS("Sports"),
    NECESSITIES("Necessities"),
    TOYS_GAMES("Toys & Games"),
    OTHER("Other");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
