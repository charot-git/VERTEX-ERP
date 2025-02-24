package com.vertex.vos.Objects;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class MemoCollectionApplication {
    private CustomerMemo customerMemo;
    private Collection collection;
    private double amount;
    private Timestamp dateLinked;
}

