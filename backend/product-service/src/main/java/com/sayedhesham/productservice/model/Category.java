package com.sayedhesham.productservice.model;

public enum Category {
    ELECTRONICS,
    CLOTHING,
    HOME_AND_KITCHEN,
    BOOKS,
    SPORTS,
    TOYS,
    BEAUTY,
    AUTOMOTIVE,
    HEALTH,
    GROCERIES,
    OTHER;

    public String toDisplayName() {
        String displayName = this.name().toLowerCase().replace("_", " ");
        return displayName.substring(0, 1).toUpperCase() + displayName.substring(1);
    }
}
