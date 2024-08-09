package com.vertex.vos.Objects;

public class Taskbar {
    private int id;
    private String taskbarCode;
    private String taskbarLabel;

    // Constructor
    public Taskbar(int id, String taskbarCode, String taskbarLabel) {
        this.id = id;
        this.taskbarCode = taskbarCode;
        this.taskbarLabel = taskbarLabel;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTaskbarCode() {
        return taskbarCode;
    }

    public void setTaskbarCode(String taskbarCode) {
        this.taskbarCode = taskbarCode;
    }

    public String getTaskbarLabel() {
        return taskbarLabel;
    }

    public void setTaskbarLabel(String taskbarLabel) {
        this.taskbarLabel = taskbarLabel;
    }

    // toString Method (optional)
    @Override
    public String toString() {
        return "Taskbar{" +
                "id=" + id +
                ", taskbarCode='" + taskbarCode + '\'' +
                ", taskbarLabel='" + taskbarLabel + '\'' +
                '}';
    }
}
