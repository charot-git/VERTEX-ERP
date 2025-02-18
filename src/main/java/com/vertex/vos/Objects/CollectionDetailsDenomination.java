package com.vertex.vos.Objects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CollectionDetailsDenomination {

    private Integer id;
    private CollectionDetail collectionDetail; // Reference to CollectionDetail
    private Denomination denomination;
    private Integer quantity;
    private Double amount;

}
