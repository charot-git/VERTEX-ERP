package com.vertex.vos;

import com.vertex.vos.Objects.Consolidation;
import com.vertex.vos.Objects.Product;
import lombok.Data;

@Data
public class ChecklistDTO {
    private Product product;
    private int orderedQuantity;
    private int servedQuantity;
}
