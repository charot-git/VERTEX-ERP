package com.vertex.vos.Objects;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Setter
@Getter
public class Operation {
    private int id;
    private String operationCode;
    private String operationName;

    public Operation() {

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
