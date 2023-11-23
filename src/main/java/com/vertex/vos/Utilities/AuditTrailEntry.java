package com.vertex.vos.Utilities;

import java.sql.Timestamp;

public class AuditTrailEntry {
    private long id;
    private Timestamp timestamp;
    private int userId;
    private String action;
    private String tableName;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public int getRecordId() {
        return recordId;
    }

    public void setRecordId(int recordId) {
        this.recordId = recordId;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    private int recordId;
    private String fieldName;
    private String oldValue;
    private String newValue;

    // Constructors, getters, and setters
    // ... (omitted for brevity)

    @Override
    public String toString() {
        return "AuditTrailEntry{" +
                "id=" + id +
                ", timestamp=" + timestamp +
                ", userId=" + userId +
                ", action='" + action + '\'' +
                ", tableName='" + tableName + '\'' +
                ", recordId=" + recordId +
                ", fieldName='" + fieldName + '\'' +
                ", oldValue='" + oldValue + '\'' +
                ", newValue='" + newValue + '\'' +
                '}';
    }
}
