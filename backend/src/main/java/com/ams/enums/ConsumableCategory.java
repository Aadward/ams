package com.ams.enums;

public enum ConsumableCategory {
    OFFICE_SUPPLIES("办公用品"),
    ELECTRONIC_PARTS("电子配件"),
    PRODUCTION_CONSUMABLES("生产耗材");

    private final String label;
    ConsumableCategory(String label) { this.label = label; }
    public String getLabel() { return label; }
}
