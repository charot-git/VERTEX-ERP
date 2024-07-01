package com.vertex.vos.Objects;

public class Vehicle {
    private int vehicleId;
    private int vehicleType;
    private String vehicleTypeString;

    public void setVehicleTypeString(String vehicleTypeString) {
        this.vehicleTypeString = vehicleTypeString;
    }

    private String vehiclePlate;
    private double maxLoad;
    private String status;

    public int getBranchId() {
        return branchId;
    }

    public void setBranchId(int branchId) {
        this.branchId = branchId;
    }

    private int branchId;

    public Vehicle(){

    }

    public int getVehicleId() {
        return vehicleId;
    }

    public Vehicle(int vehicleId, int vehicleType, String vehicleTypeString, String vehiclePlate, double maxLoad, String status, int branchId) {
        this.vehicleId = vehicleId;
        this.vehicleType = vehicleType;
        this.vehicleTypeString = vehicleTypeString;
        this.vehiclePlate = vehiclePlate;
        this.maxLoad = maxLoad;
        this.status = status;
        this.branchId = branchId;
    }

    public void setVehicleId(int vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getVehicleTypeString() {
        return vehicleTypeString;
    }

    public int getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(int vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getVehiclePlate() {
        return vehiclePlate;
    }

    public void setVehiclePlate(String vehiclePlate) {
        this.vehiclePlate = vehiclePlate;
    }

    public double getMaxLoad() {
        return maxLoad;
    }

    public void setMaxLoad(double maxLoad) {
        this.maxLoad = maxLoad;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
