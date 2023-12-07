package com.vertex.vos.Constructors;

public class BSIS {
    private int id;
    private String bsisCode;
    private String bsisName;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBsisCode() {
        return bsisCode;
    }

    public void setBsisCode(String bsisCode) {
        this.bsisCode = bsisCode;
    }

    public String getBsisName() {
        return bsisName;
    }

    public void setBsisName(String bsisName) {
        this.bsisName = bsisName;
    }

    public BSIS(int id, String bsisCode, String bsisName) {
        this.id = id;
        this.bsisCode = bsisCode;
        this.bsisName = bsisName;
    }


}
