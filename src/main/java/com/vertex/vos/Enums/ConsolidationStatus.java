package com.vertex.vos.Enums;

public enum ConsolidationStatus {
    PENDING("Pending"), PICKING("Picking"), PICKED("Picked");

    private final String value;

    ConsolidationStatus(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

    public static ConsolidationStatus fromString(String status) {
        for (ConsolidationStatus cs : ConsolidationStatus.values()) {
            if (cs.value.equalsIgnoreCase(status)) {
                return cs;
            }
        }
        throw new IllegalArgumentException("Invalid DispatchStatus: " + status);
    }
}
