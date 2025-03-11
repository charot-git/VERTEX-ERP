package com.vertex.vos.Objects;

import com.vertex.vos.Enums.PickListStatus;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Data;

import java.sql.Timestamp;

@Data
public class PickList {
    private int id;
    private String pickNo;
    private User pickedBy;
    private User createdBy;
    private Timestamp pickDate;
    private Branch branch; // Nullable field
    private PickListStatus status;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private boolean isPrinted;

    ObservableList<PickListItem> pickListItems = FXCollections.observableArrayList();

}
