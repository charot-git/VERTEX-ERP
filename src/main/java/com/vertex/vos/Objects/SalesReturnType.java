package com.vertex.vos.Objects;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
@Getter
@Setter
public class SalesReturnType {
    private int typeId;
    private String typeName;
    private String description;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}
