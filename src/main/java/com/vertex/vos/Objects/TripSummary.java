package com.vertex.vos.Objects;

import javafx.beans.DefaultProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Data;

import java.sql.Date;
import java.sql.Timestamp;
@Data
public class TripSummary {
    private int tripId;
    private String tripNo;
    private Timestamp tripDate;
    private Vehicle vehicle;
    private TripStatus status;
    private Timestamp createdAt;
    private User createdBy;
    private User dispatchBy;
    private double tripAmount;
    private Cluster cluster;

    public enum TripStatus {
        Allotment, Picking, Picked, Dispatched
    }

    ObservableList<SalesInvoiceHeader> salesInvoices = FXCollections.observableArrayList();
    ObservableList<TripSummaryStaff> tripSummaryStaffs = FXCollections.observableArrayList();

}
