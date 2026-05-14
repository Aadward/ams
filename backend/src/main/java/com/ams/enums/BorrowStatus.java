package com.ams.enums;

public enum BorrowStatus {
    BORROWED("已借出"),
    RETURNED("已归还"),
    OVERDUE("已超期");

    private final String label;

    BorrowStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
