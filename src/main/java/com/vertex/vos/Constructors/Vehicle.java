package com.vertex.vos.Constructors;

public class Vehicle {
    private int vehicleId;
    private String vehicleType;
    private String vehiclePlate;
    private double maxLoad;
    private String status;

    public Vehicle(){

    }

    public int getVehicleId() {
        return vehicleId;
    }

    public Vehicle(int vehicleId, String vehicleType, String vehiclePlate, double maxLoad, String status) {
        this.vehicleId = vehicleId;
        this.vehicleType = vehicleType;
        this.vehiclePlate = vehiclePlate;
        this.maxLoad = maxLoad;
        this.status = status;
    }

    public void setVehicleId(int vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
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
