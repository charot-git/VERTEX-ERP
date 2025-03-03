package com.vertex.vos.Objects;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.sql.Timestamp;

@Data // Generates getters, setters, toString, equals, and hashCode
@NoArgsConstructor // Generates a no-args constructor
@AllArgsConstructor // Generates an all-args constructor
public class Collection {

    private Integer id; // Corresponds to `id` INT(10) NOT NULL AUTO_INCREMENT

    private String docNo; // Corresponds to `docNo` VARCHAR(255)

    private Timestamp collectionDate; // Corresponds to `collection_date` TIMESTAMP

    private Timestamp dateEncoded; // Corresponds to `date_encoded` TIMESTAMP

    private Salesman salesman; // Corresponds to `salesman_id` INT(10)

    private User collectedBy; // Corresponds to `collected_by` INT(10)

    private User encoderId; // Corresponds to `encoder_id` INT(10)

    private String remarks; // Corresponds to `remarks` TEXT

    private Boolean isPosted; // Corresponds to `isPosted` BIT(1)

    private Boolean isCancelled; // Corresponds to `isCancelled` BIT(1)

    private Double totalAmount; // Corresponds to `totalAmount` DOUBLE

    private ObservableList<SalesInvoiceHeader> salesInvoiceHeaders = FXCollections.observableArrayList(); // Corresponds to `salesInvoiceHeaders`
    private ObservableList<MemoCollectionApplication> customerMemos = FXCollections.observableArrayList();  // Corresponds to `customerCreditDebitMemos`
    private ObservableList<CollectionDetail> collectionDetails = FXCollections.observableArrayList(); // Corresponds to `collectionDetails`>
    private ObservableList<SalesReturn> salesReturns = FXCollections.observableArrayList();  // Corresponds to `salesReturns`
}