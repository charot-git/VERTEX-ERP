package com.vertex.vos.Objects;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
    private Salesman salesman;
    private Timestamp returnDate;
    private double totalAmount;
    private double discountAmount;
    private double grossAmount;
    private String remarks;
    private int createdBy;
    private int sales_invoice_id;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private Timestamp receivedAt;
    private String status;
    private boolean isThirdParty;
    private String priceType;
    private boolean isPosted;
    private boolean isReceived;
    private ObservableList<SalesReturnDetail> salesReturnDetails = FXCollections.observableArrayList();


}
