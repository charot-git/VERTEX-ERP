package com.vertex.vos.Objects;

import lombok.Data;

@Data
public class Cluster {
    private int id;
    private String clusterName;
    private double minimumAmount;

    public Cluster() {
    }



}
