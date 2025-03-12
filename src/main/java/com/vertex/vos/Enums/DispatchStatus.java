package com.vertex.vos.Enums;

public enum DispatchStatus {
    PENDING("Pending"),
    PICKING("Picking"),
    PICKED("Picked"),
    DISPATCHED("Dispatched");

    private final String value;

    DispatchStatus(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

    public static DispatchStatus fromString(String status) {
        for (DispatchStatus ds : DispatchStatus.values()) {
            if (ds.value.equalsIgnoreCase(status)) {
                return ds;
            }
        }
        throw new IllegalArgumentException("Invalid DispatchStatus: " + status);
    }
}
