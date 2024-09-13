package com.vertex.vos.Objects;

import com.vertex.vos.Utilities.DisplayName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Getter
@Setter
@AllArgsConstructor
public class BankAccount {

    @DisplayName(value = "Bank ID", exclude = true)
    private int bankId;

    @DisplayName("Bank Name")
    private String bankName;

    @DisplayName("Account Number")
    private String accountNumber;

    @DisplayName("Description")
    private String bankDescription;

    @DisplayName("Branch")
    private String branch;

    @DisplayName(value = "IFSC Code", exclude = true)
    private String ifscCode;

    @DisplayName("Opening Balance")
    private BigDecimal openingBalance;

    @DisplayName("Province")
    private String province;

    @DisplayName("City")
    private String city;

    @DisplayName("Baranggay")
    private String baranggay;

    @DisplayName("Email")
    private String email;

    @DisplayName("Mobile No.")
    private String mobileNo;

    @DisplayName("Contact Person")
    private String contactPerson;

    @DisplayName("Active Status")
    private boolean isActive;

    @DisplayName("Created At")
    private Timestamp createdAt;

    @DisplayName(value = "Created By", exclude = true)
    private int createdBy;

    public BankAccount() {
    }
}
