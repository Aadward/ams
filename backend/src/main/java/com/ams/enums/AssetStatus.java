package com.ams.enums;

public enum AssetStatus {
    IN_STOCK("库存"),
    IN_USE("已领用"),
    MAINTENANCE("维修中"),
    RETIRED("已报废");

    private final String label;

    AssetStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
