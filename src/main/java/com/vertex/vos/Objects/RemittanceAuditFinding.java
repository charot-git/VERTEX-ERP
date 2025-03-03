package com.vertex.vos.Objects;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RemittanceAuditFinding {
    private Integer id; // Primary Key
    private String docNo; // Document Number
    private Timestamp dateAudited;
    private Timestamp dateFrom;
    private Timestamp dateTo;
    private Timestamp dateCreated;
    private Timestamp dateUpdated;
    private Double amount;

    private Salesman auditee;
    private User auditor;

    // List of related CollectionDetail records
    private ObservableList<CollectionDetail> collectionDetails = FXCollections.observableArrayList();
}
