package com.vertex.vos.Constructors;

import java.sql.Date;

public class Industry {
    private int id;
    private String industryName;
    private String industryHead;
    private String industryDescription;
    private Date dateAdded;
    private int taxId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIndustryName() {
        return industryName;
    }

    public void setIndustryName(String industryName) {
        this.industryName = industryName;
    }

    public String getIndustryHead() {
        return industryHead;
    }

    public void setIndustryHead(String industryHead) {
        this.industryHead = industryHead;
    }

    public String getIndustryDescription() {
        return industryDescription;
    }

    public void setIndustryDescription(String industryDescription) {
        this.industryDescription = industryDescription;
    }

    public Date getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(Date dateAdded) {
        this.dateAdded = dateAdded;
    }

    public int getTaxId() {
        return taxId;
    }

    public void setTaxId(int taxId) {
        this.taxId = taxId;
    }

    public Industry(int id, String industryName, String industryHead, String industryDescription, Date dateAdded, int taxId) {
        this.id = id;
        this.industryName = industryName;
        this.industryHead = industryHead;
        this.industryDescription = industryDescription;
        this.dateAdded = dateAdded;
        this.taxId = taxId;
    }
}
