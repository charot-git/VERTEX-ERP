package com.vertex.vos.Constructors;

public class VersionControl {
    private int id;
    private String versionName;
    private boolean active;

    public VersionControl() {
        // Default constructor
    }

    public VersionControl(int id, String versionName, boolean active) {
        this.id = id;
        this.versionName = versionName;
        this.active = active;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
