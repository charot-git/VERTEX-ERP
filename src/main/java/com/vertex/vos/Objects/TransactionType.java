package com.vertex.vos.Objects;

public class TransactionType {
    private int id;
    private String transactionTypeName;

    public TransactionType(int id, String transactionTypeName) {
        this.id = id;
        this.transactionTypeName = transactionTypeName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTransactionTypeName(String transactionTypeName) {
        this.transactionTypeName = transactionTypeName;
    }

    public String getTransactionTypeName() {
        return transactionTypeName;
    }
}
