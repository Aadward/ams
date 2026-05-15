package com.ams.enums;

public enum SupplierType {
    EQUIPMENT("设备"),
    CONSUMABLE("耗材"),
    MAINTENANCE("维护"),
    MULTI("综合");

    private final String label;

    SupplierType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
