package com.vertex.vos.Constructors;

public class Category {
    private int category_id;
    private String category_name;

    public Category(int category_id, String category_name) {
        this.category_id = category_id;
        this.category_name = category_name;
    }

    public int getCategory_id() {
        return category_id;
    }

    public String getCategory_name() {
        return category_name;
    }
}
