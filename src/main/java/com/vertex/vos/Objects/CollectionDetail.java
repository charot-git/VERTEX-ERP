package com.vertex.vos.Objects;

import javafx.collections.ObservableList;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.sql.Timestamp;

@Data // Generates getters, setters, toString, equals, and hashCode
@NoArgsConstructor // Generates a no-args constructor
@AllArgsConstructor // Generates an all-args constructor
public class CollectionDetail {

    private Integer id; // Corresponds to `id` INT(10) NOT NULL AUTO_INCREMENT

    private Integer collectionId; // Corresponds to `collection_id` INT(10)

    private ChartOfAccounts type; // Corresponds to `type` INT(10)

    private BankName bank; // Corresponds to `bank` INT(10)

    private String checkNo; // Corresponds to `check_no` INT(10)

    private Double amount; // Corresponds to `amount` DOUBLE

    private String remarks; // Corresponds to `remarks` TEXT

    private int encoderId;
    private Timestamp checkDate;

    private ObservableList<CollectionDetailsDenomination> denominations; // <Denomination>

    private boolean isPayment;
}