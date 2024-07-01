package com.vertex.vos.Objects;

public class TripSummaryDetails {
    private int detailId;
    private int tripId;
    private int orderId;

    public TripSummaryDetails() {
        // Default constructor
    }

    public TripSummaryDetails(int detailId, int tripId, int orderId) {
        this.detailId = detailId;
        this.tripId = tripId;
        this.orderId = orderId;
    }

    public int getDetailId() {
        return detailId;
    }

    public void setDetailId(int detailId) {
        this.detailId = detailId;
    }

    public int getTripId() {
        return tripId;
    }

    public void setTripId(int tripId) {
        this.tripId = tripId;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    @Override
    public String toString() {
        return "TripSummaryDetails{" +
                "detailId=" + detailId +
                ", tripId=" + tripId +
                ", orderId=" + orderId +
                '}';
    }
}
