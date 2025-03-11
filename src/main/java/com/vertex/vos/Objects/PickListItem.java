package com.vertex.vos.Objects;

import com.vertex.vos.Enums.PickListItemStatus;
import com.vertex.vos.Enums.DocumentType;
import lombok.Data;

import java.sql.ResultSet;
import java.sql.Timestamp;

@Data
public class PickListItem {
    private int id;
    private PickList pickList;
    private DocumentType docType;
    private String docNo;
    private Product product;
    private int orderedQuantity;
    private int pickedQuantity;
    private PickListItemStatus status;
    private Timestamp createdAt;
    private Timestamp updatedAt;

}
