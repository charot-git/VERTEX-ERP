package com.vertex.vos.Constructors;

import java.sql.Date;
import java.sql.Timestamp;

public class TripSummary {
    private int tripId;
    private String tripNo;
    private Date tripDate;
    private int vehicleId;
    private int totalSalesOrders;
    private String status;
    private Timestamp createdAt;
    private int createdBy;
    private int dispatchBy;

    public TripSummary(int tripId, String tripNo, Date tripDate, int vehicleId, int totalSalesOrders, String status, Timestamp createdAt, int createdBy, int dispatchBy) {
        this.tripId = tripId;
        this.tripNo = tripNo;
        this.tripDate = tripDate;
        this.vehicleId = vehicleId;
        this.totalSalesOrders = totalSalesOrders;
        this.status = status;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        this.dispatchBy = dispatchBy;
    }

    public TripSummary() {

    }

    // Getters and setters for all fields

    public int getTripId() {
        return tripId;
    }

    public void setTripId(int tripId) {
        this.tripId = tripId;
    }

    public String getTripNo() {
        return tripNo;
    }

    public void setTripNo(String tripNo) {
        this.tripNo = tripNo;
    }

    public Date getTripDate() {
        return tripDate;
    }

    public void setTripDate(Date tripDate) {
        this.tripDate = tripDate;
    }

    public int getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(int vehicleId) {
        this.vehicleId = vehicleId;
    }

    public int getTotalSalesOrders() {
        return totalSalesOrders;
    }

    public void setTotalSalesOrders(int totalSalesOrders) {
        this.totalSalesOrders = totalSalesOrders;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }

    public int getDispatchBy() {
        return dispatchBy;
    }

    public void setDispatchBy(int dispatchBy) {
        this.dispatchBy = dispatchBy;
    }
}
