package com.vertex.vos.Objects;

import lombok.Getter;
import lombok.Setter;

import java.sql.Date;

@Setter
@Getter
public class Division {
    private int id;
    private String divisionName;

    private String divisionHead;

    public Division(int id, String divisionName, String divisionHead, String divisionDescription, String divisionCode, Date dateAdded) {
        this.id = id;
        this.divisionName = divisionName;
        this.divisionHead = divisionHead;
        this.divisionDescription = divisionDescription;
        this.divisionCode = divisionCode;
        this.dateAdded = dateAdded;
    }

    private String divisionDescription;
    private String divisionCode;
    private Date dateAdded;

    public Division() {

    }


}
