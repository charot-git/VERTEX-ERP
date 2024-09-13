package com.vertex.vos.Objects;

import lombok.Getter;
import lombok.Setter;

import java.sql.Date;

@Setter
@Getter
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


}
