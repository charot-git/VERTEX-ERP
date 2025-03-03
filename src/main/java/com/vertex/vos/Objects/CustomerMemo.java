package com.vertex.vos.Objects;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Data;

import java.sql.Timestamp;

@Data
public class CustomerMemo {
    private int id;
    private String memoNumber;
    private Supplier supplier;
    private BalanceType balanceType;
    private Customer customer;
    private Salesman salesman;
    private double amount;
    private double appliedAmount;
    private String reason;
    private MemoStatus status;
    private ChartOfAccounts chartOfAccount;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private int encoderId;
    private Boolean isPending;

    private ObservableList<SalesInvoiceHeader> salesInvoiceHeaders = FXCollections.observableArrayList();
    private ObservableList<Collection> collections = FXCollections.observableArrayList();

    private ObservableList<MemoInvoiceApplication> invoiceApplications = FXCollections.observableArrayList();
    private ObservableList<MemoCollectionApplication> collectionApplications = FXCollections.observableArrayList();

    public enum MemoStatus {
        FOR_APPROVAL,
        PARTIALLY_APPLIED,
        APPLIED,
        APPROVED;

        // Convert to a space-separated string
        public String toDisplayString() {
            return this.name().replace("_", " ");
        }

        // Convert from a database string
        public static MemoStatus fromDbValue(String dbValue) {
            return MemoStatus.valueOf(dbValue.replace(" ", "_"));
        }
    }

}
