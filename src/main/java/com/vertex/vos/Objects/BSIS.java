package com.vertex.vos.Objects;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class BSIS {
    private int id;
    private String bsisCode;
    private String bsisName;

    public BSIS(int id, String bsisCode, String bsisName) {
        this.id = id;
        this.bsisCode = bsisCode;
        this.bsisName = bsisName;
    }


}
