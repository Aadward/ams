package com.ams.enums;

public enum ConsumableRecordType {
    IN("入库"),
    OUT("出库");

    private final String label;
    ConsumableRecordType(String label) { this.label = label; }
    public String getLabel() { return label; }
}
