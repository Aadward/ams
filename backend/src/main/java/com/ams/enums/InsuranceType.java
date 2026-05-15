package com.ams.enums;

public enum InsuranceType {
    PROPERTY("财产险"),
    COMPREHENSIVE("综合险"),
    THEFT("盗抢险");

    private final String description;

    InsuranceType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
