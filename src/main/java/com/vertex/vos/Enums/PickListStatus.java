package com.vertex.vos.Enums;

public enum PickListStatus {
    PENDING("Pending"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled");

    private final String status;

    PickListStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return status;
    }
}
