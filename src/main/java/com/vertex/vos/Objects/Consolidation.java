package com.vertex.vos.Objects;

import com.vertex.vos.Enums.ConsolidationStatus;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Data;

import java.sql.Timestamp;

@Data
public class Consolidation {
    int id;
    String consolidationNo;
    ConsolidationStatus status;
    User createdBy;
    User checkedBy;
    Timestamp createdAt;
    Timestamp updatedAt;

    ObservableList<DispatchPlan> dispatchPlans = FXCollections.observableArrayList();
    ObservableList<StockTransfer> stockTransfers = FXCollections.observableArrayList();
    ObservableList<ConsolidationDetails> consolidationDetails = FXCollections.observableArrayList();
}
