package com.vertex.vos.Objects;

import lombok.*;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesReturn {
    private int returnId;
    private String returnNumber;
    private String customerCode;
    private Customer customer;
    private Timestamp returnDate;
    private double totalAmount;  // Changed to BigDecimal for currency
    private String remarks;
    private int createdBy;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private String status;
    private boolean isThirdParty;
    private String priceType;
    private boolean isPosted;


}
