package com.vertex.vos.Objects;

public class InvoiceType {
    private int id;
    private String type;

    public InvoiceType(int id, String type) {
        this.id = id;
        this.type = type;
    }

    public InvoiceType() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
