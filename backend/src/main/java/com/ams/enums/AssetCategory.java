package com.ams.enums;

public enum AssetCategory {
    HARDWARE("硬件设备"),
    NETWORK("网络设备"),
    PERIPHERAL("配件耗材"),
    SOFTWARE_LICENSE("软件许可证");

    private final String label;

    AssetCategory(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
