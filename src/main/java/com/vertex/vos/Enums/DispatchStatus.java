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
    
}