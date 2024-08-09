package com.vertex.vos.Objects;

public class Module {
    private int id;
    private int taskbarId;
    private String moduleCode;
    private String moduleLabel;

    // Constructor
    public Module(int id, int taskbarId, String moduleCode, String moduleLabel) {
        this.id = id;
        this.taskbarId = taskbarId;
        this.moduleCode = moduleCode;
        this.moduleLabel = moduleLabel;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTaskbarId() {
        return taskbarId;
    }

    public void setTaskbarId(int taskbarId) {
        this.taskbarId = taskbarId;
    }

    public String getModuleCode() {
        return moduleCode;
    }

    public void setModuleCode(String moduleCode) {
        this.moduleCode = moduleCode;
    }

    public String getModuleLabel() {
        return moduleLabel;
    }

    public void setModuleLabel(String moduleLabel) {
        this.moduleLabel = moduleLabel;
    }

    // toString Method (optional)
    @Override
    public String toString() {
        return "Module{" +
                "id=" + id +
                ", taskbarId=" + taskbarId +
                ", moduleCode='" + moduleCode + '\'' +
                ", moduleLabel='" + moduleLabel + '\'' +
                '}';
    }
}
