package com.vertex.vos.Constructors;

import java.sql.Timestamp;

public class Operation {
    private int id;
    private String operationCode;
    private String operationName;

    public Operation() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getOperationCode() {
        return operationCode;
    }

    public void setOperationCode(String operationCode) {
        this.operationCode = operationCode;
    }

    public String getOperationName() {
        return operationName;
    }

    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }

    public Timestamp getDateModified() {
        return dateModified;
    }

    public void setDateModified(Timestamp dateModified) {
        this.dateModified = dateModified;
    }

    public int getEncoderId() {
        return encoderId;
    }

    public void setEncoderId(int encoderId) {
        this.encoderId = encoderId;
    }

    public int getCompanyId() {
        return companyId;
    }

    public void setCompanyId(int companyId) {
        this.companyId = companyId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public Operation(int id, String operationCode, String operationName, Timestamp dateModified, int encoderId, int companyId, int type, String definition) {
        this.id = id;
        this.operationCode = operationCode;
        this.operationName = operationName;
        this.dateModified = dateModified;
        this.encoderId = encoderId;
        this.companyId = companyId;
        this.type = type;
        this.definition = definition;
    }

    private Timestamp dateModified;
    private int encoderId;
    private int companyId;
    private int type;
    private String definition;
}
