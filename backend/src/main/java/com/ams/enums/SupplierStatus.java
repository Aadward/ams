package com.ams.enums;

public enum SupplierStatus {
    ACTIVE("活跃"),
    INACTIVE("不活跃");

    private final String label;

    SupplierStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
