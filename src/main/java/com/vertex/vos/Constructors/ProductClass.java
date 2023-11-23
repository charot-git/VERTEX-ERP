package com.vertex.vos.Constructors;

public class ProductClass {
    private int classId;
    private String className;

    // Constructor
    public ProductClass(int classId, String className) {
        this.classId = classId;
        this.className = className;
    }

    // Getters and setters
    public int getClassId() {
        return classId;
    }

    public void setClassId(int classId) {
        this.classId = classId;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }
}
