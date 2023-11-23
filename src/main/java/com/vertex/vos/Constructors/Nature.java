package com.vertex.vos.Constructors;

public class Nature {
    private int natureId;
    private String natureName;

    // Constructor
    public Nature(int natureId, String natureName) {
        this.natureId = natureId;
        this.natureName = natureName;
    }

    // Getters and setters
    public int getNatureId() {
        return natureId;
    }

    public void setNatureId(int natureId) {
        this.natureId = natureId;
    }

    public String getNatureName() {
        return natureName;
    }

    public void setNatureName(String natureName) {
        this.natureName = natureName;
    }
}
