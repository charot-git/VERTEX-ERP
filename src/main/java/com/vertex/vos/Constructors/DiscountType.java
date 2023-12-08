package com.vertex.vos.Constructors;

public class DiscountType {
    private String typeName;
    private int id;

    // Constructor
    public DiscountType(String typeName, int id) {
        this.typeName = typeName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public DiscountType(int id) {
        this.id = id;
    }

    // Getter and setter for typeName
    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }
}
