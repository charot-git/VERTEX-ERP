package com.vertex.vos.Objects;

import java.sql.Date;

public class Department {
    private int departmentId;
    private String parentDivision;
    private String departmentName;
    private String departmentHead;
    private String departmentDescription;
    private Date dateAdded;

    public Department(int departmentId, String parentDivision, String departmentName, String departmentHead, String departmentDescription, int taxId, Date dateAdded) {
        this.departmentId = departmentId;
        this.parentDivision = parentDivision;
        this.departmentName = departmentName;
        this.departmentHead = departmentHead;
        this.departmentDescription = departmentDescription;
        this.dateAdded = dateAdded;
        this.taxId = taxId;
    }

    private int taxId;


    public int getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(int departmentId) {
        this.departmentId = departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public String getParentDivision() {
        return parentDivision;
    }

    public void setParentDivision(String parentDivision) {
        this.parentDivision = parentDivision;
    }

    public String getDepartmentHead() {
        return departmentHead;
    }

    public void setDepartmentHead(String departmentHead) {
        this.departmentHead = departmentHead;
    }

    public String getDepartmentDescription() {
        return departmentDescription;
    }

    public void setDepartmentDescription(String departmentDescription) {
        this.departmentDescription = departmentDescription;
    }

    public int getTaxId() {
        return taxId;
    }

    public void setTaxId(int taxId) {
        this.taxId = taxId;
    }

    public Date getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(Date dateAdded) {
        this.dateAdded = dateAdded;
    }
}
