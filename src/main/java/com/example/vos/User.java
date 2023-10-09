package com.example.vos;

import java.sql.Date;

public class User {
    private int user_id;
    private String user_email;
    private String user_password;
    private String user_fname;
    private String user_mname;
    private String user_lname;
    private String user_contact;
    private String user_province;
    private String user_city;
    private String user_brgy;
    private String user_sss;
    private String user_philhealth;
    private String user_tin;
    private String user_position;
    private String user_department;
    private String user_tags;

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getUser_email() {
        return user_email;
    }

    public void setUser_email(String user_email) {
        this.user_email = user_email;
    }

    public String getUser_password() {
        return user_password;
    }

    public void setUser_password(String user_password) {
        this.user_password = user_password;
    }

    public String getUser_fname() {
        return user_fname;
    }

    public void setUser_fname(String user_fname) {
        this.user_fname = user_fname;
    }

    public String getUser_mname() {
        return user_mname;
    }

    public void setUser_mname(String user_mname) {
        this.user_mname = user_mname;
    }

    public String getUser_lname() {
        return user_lname;
    }

    public void setUser_lname(String user_lname) {
        this.user_lname = user_lname;
    }

    public String getUser_contact() {
        return user_contact;
    }

    public void setUser_contact(String user_contact) {
        this.user_contact = user_contact;
    }

    public String getUser_province() {
        return user_province;
    }

    public void setUser_province(String user_province) {
        this.user_province = user_province;
    }

    public String getUser_city() {
        return user_city;
    }

    public void setUser_city(String user_city) {
        this.user_city = user_city;
    }

    public String getUser_brgy() {
        return user_brgy;
    }

    public void setUser_brgy(String user_brgy) {
        this.user_brgy = user_brgy;
    }

    public String getUser_sss() {
        return user_sss;
    }

    public void setUser_sss(String user_sss) {
        this.user_sss = user_sss;
    }

    public String getUser_philhealth() {
        return user_philhealth;
    }

    public void setUser_philhealth(String user_philhealth) {
        this.user_philhealth = user_philhealth;
    }

    public String getUser_tin() {
        return user_tin;
    }

    public void setUser_tin(String user_tin) {
        this.user_tin = user_tin;
    }

    public String getUser_position() {
        return user_position;
    }

    public void setUser_position(String user_position) {
        this.user_position = user_position;
    }

    public String getUser_department() {
        return user_department;
    }

    public void setUser_department(String user_department) {
        this.user_department = user_department;
    }

    public String getUser_tags() {
        return user_tags;
    }

    public void setUser_tags(String user_tags) {
        this.user_tags = user_tags;
    }

    public Date getUser_dateOfHire() {
        return user_dateOfHire;
    }

    public void setUser_dateOfHire(Date user_dateOfHire) {
        this.user_dateOfHire = user_dateOfHire;
    }

    public Date getUser_bday() {
        return user_bday;
    }

    public void setUser_bday(Date user_bday) {
        this.user_bday = user_bday;
    }

    private Date user_dateOfHire;
    private Date user_bday;
}
