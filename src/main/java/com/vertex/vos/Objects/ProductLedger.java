package com.vertex.vos.Objects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ProductLedger {
    int id;
    Branch branch;
    Product product;
    int quantity;
    String documentNo;
    Date documentDate;
    String documentType;
    String documentDescription;
    int in;
    int out;
}
