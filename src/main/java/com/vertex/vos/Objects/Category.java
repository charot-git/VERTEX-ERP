package com.vertex.vos.Objects;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Category {
    private final int category_id;
    private final String category_name;

    public Category(int category_id, String category_name) {
        this.category_id = category_id;
        this.category_name = category_name;
    }

}
