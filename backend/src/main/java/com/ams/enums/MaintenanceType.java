package com.ams.enums;

public enum MaintenanceType {
    REPAIR("维修"),
    MAINTENANCE("保养"),
    INSPECTION("巡检");

    private final String label;

    MaintenanceType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
