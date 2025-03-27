package com.vertex.vos;

import com.vertex.vos.Objects.Consolidation;
import com.vertex.vos.Objects.Product;
import com.vertex.vos.Objects.User;
import com.vertex.vos.Objects.UserSession;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConsolidationDetails {
    private Consolidation consolidation;
    private Product product;
    private int orderedQuantity;
    private int pickedQuantity;
    private Timestamp pickedAt;
    private User pickedBy;
}
