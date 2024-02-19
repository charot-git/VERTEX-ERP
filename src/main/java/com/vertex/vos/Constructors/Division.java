package com.vertex.vos.Constructors;

import java.sql.Date;

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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDivisionName() {
        return divisionName;
    }

    public void setDivisionName(String divisionName) {
        this.divisionName = divisionName;
    }

    public String getDivisionDescription() {
        return divisionDescription;
    }

    public void setDivisionDescription(String divisionDescription) {
        this.divisionDescription = divisionDescription;
    }

    public String getDivisionHead() {
        return divisionHead;
    }

    public void setDivisionHead(String divisionHead) {
        this.divisionHead = divisionHead;
    }

    public String getDivisionCode() {
        return divisionCode;
    }

    public void setDivisionCode(String divisionCode) {
        this.divisionCode = divisionCode;
    }

    public Date getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(Date dateAdded) {
        this.dateAdded = dateAdded;
    }


}
