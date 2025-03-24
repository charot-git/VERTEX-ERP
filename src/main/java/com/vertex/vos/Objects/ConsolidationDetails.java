package com.vertex.vos.Objects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConsolidationDetails {
    Consolidation consolidation;
    Product product;
    int orderedQuantity;
    int receivedQuantity;
    Timestamp pickedAt;
    User pickedBy;
}
