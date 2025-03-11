package com.vertex.vos.Objects;

import com.vertex.vos.Enums.DispatchStatus;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Data;
import lombok.Getter;

import java.sql.Timestamp;

@Data
public class DispatchPlan {
    private int dispatchId;
    private String dispatchNo;
    private Timestamp dispatchDate;
    private Vehicle vehicle;
    private Timestamp createdAt;
    private User createdBy;
    private DispatchStatus status;
    private Double totalAmount;
    private Cluster cluster;

    // Use encapsulation with a getter method
    @Getter
    private final ObservableList<DispatchPlanDetails> dispatchPlanDetails = FXCollections.observableArrayList();

}
