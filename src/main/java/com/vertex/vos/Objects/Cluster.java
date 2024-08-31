package com.vertex.vos.Objects;

public class Cluster {
    private int id;
    private String clusterName;
    private double minimumAmount;

    public Cluster() {
    }

    public Cluster(int id, String clusterName, double minimumAmount) {
        this.id = id;
        this.clusterName = clusterName;
        this.minimumAmount = minimumAmount;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public double getMinimumAmount() {
        return minimumAmount;
    }

    public void setMinimumAmount(double minimumAmount) {
        this.minimumAmount = minimumAmount;
    }
}
