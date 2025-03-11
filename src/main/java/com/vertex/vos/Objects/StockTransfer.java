package com.vertex.vos.Objects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class StockTransfer {
    private String orderNo;
    private int sourceBranch;
    private int targetBranch;
    private int productId;
    private Product product;
    private int orderedQuantity;
    private int receivedQuantity;
    private double amount;
    private java.sql.Date dateRequested;
    private java.sql.Date leadDate;
    private String status;
    private Timestamp dateReceived;
    private int receiverId;
    private int encoderId;
}
