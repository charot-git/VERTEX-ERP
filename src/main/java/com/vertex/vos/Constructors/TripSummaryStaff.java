package com.vertex.vos.Constructors;

public class TripSummaryStaff {
    private int staffId;
    private int tripId;
    private String staffName;
    private String role; // "Driver" or "Helper"

    // Constructors
    public TripSummaryStaff() {
    }

    public TripSummaryStaff(int staffId, int tripId, String staffName, String role) {
        this.staffId = staffId;
        this.tripId = tripId;
        this.staffName = staffName;
        this.role = role;
    }

    // Getters and Setters
    public int getStaffId() {
        return staffId;
    }

    public void setStaffId(int staffId) {
        this.staffId = staffId;
    }

    public int getTripId() {
        return tripId;
    }

    public void setTripId(int tripId) {
        this.tripId = tripId;
    }

    public String getStaffName() {
        return staffName;
    }

    public void setStaffName(String staffName) {
        this.staffName = staffName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    // toString method for debugging or logging
    @Override
    public String toString() {
        return "TripSummaryStaff{" +
                "staffId=" + staffId +
                ", tripId=" + tripId +
                ", staffName='" + staffName + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}
