package com.vertex.vos.Objects;

import lombok.*;

import java.sql.Date;
@Data
@NoArgsConstructor
@AllArgsConstructor
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
    private int user_department;
    private String userDepartmentString;

    private String user_tags;

    public User(int userId, String userEmail, String userFname, String userMname, String userLname, String userContact, String userProvince, String userCity, String userBrgy, String userPosition, int userDepartment, String userDepatmentString, String userTags, Date userBday, int roleId, String userImage) {
        this.user_id = userId;
        this.user_email = userEmail;
        this.user_fname = userFname;
        this.user_mname = userMname;
        this.user_lname = userLname;
        this.user_contact = userContact;
        this.user_province = userProvince;
        this.user_city = userCity;
        this.user_brgy = userBrgy;
        this.user_position = userPosition;
        this.user_department = userDepartment;
        this.userDepartmentString = userDepatmentString;
        this.user_bday = userBday;
        this.user_tags = userTags;
        this.user_image = userImage;
    }

    private String user_image;

    public User(int userId, String userEmail, String userPassword, String userFname, String userMname, String userLname, String userContact, String userProvince, String userCity, String userBrgy, String userSss, String userPhilhealth, String userTin, String userPosition, int userDepartment, String userDepatmentString , Date userDateOfHire, String userTags, Date userBday, int roleId, String userImages) {
        this.user_id = userId;
        this.user_email = userEmail;
        this.user_password = userPassword;
        this.user_fname = userFname;
        this.user_mname = userMname;
        this.user_lname = userLname;
        this.user_contact = userContact;
        this.user_province = userProvince;
        this.user_city = userCity;
        this.user_brgy = userBrgy;
        this.user_sss = userSss;
        this.user_philhealth = userPhilhealth;
        this.user_tin = userTin;
        this.user_position = userPosition;
        this.user_department = userDepartment;
        this.userDepartmentString = userDepatmentString;
        this.user_dateOfHire = userDateOfHire;
        this.user_tags = userTags;
        this.user_bday = userBday;
        this.user_image = userImages;
        // Note: roleId and userImages are not being used in the constructor, consider using them if needed.
    }

    private Date user_dateOfHire;
    private Date user_bday;

    private String lastMessage;

}
