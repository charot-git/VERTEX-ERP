package com.vertex.vos.Objects;

import com.vertex.vos.DAO.ClusterDAO;
import com.vertex.vos.Utilities.DisplayName;
import lombok.*;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@Setter
@Getter
@AllArgsConstructor
public class Customer {

    @DisplayName(value = "Customer ID", exclude = true)
    private int customerId;
    @DisplayName(value = "Customer Code")
    private String customerCode;
    @DisplayName(value = "Customer Name")
    private String customerName;
    @DisplayName(value = "Customer Image", exclude = true)
    private String customerImage;
    @DisplayName(value = "Store Name")
    private String storeName;
    @DisplayName(value = "Store Signage")
    private String storeSignage;
    @DisplayName(value = "Baranggayy")
    private String brgy;
    @DisplayName(value = "City")
    private String city;
    @DisplayName(value = "Province")
    private String province;
    @DisplayName(value = "Contact Number")
    private String contactNumber;
    @DisplayName(value = "Email Address")
    private String customerEmail;
    @DisplayName(value = "Tel. Number")
    private String telNumber;
    @DisplayName(value = "TIN", exclude = true)
    private String customerTin;
    @DisplayName(value = "Payment Term", exclude = true)
    private byte paymentTerm;
    @DisplayName(value = "Store Type", exclude = true)
    private int storeType;
    @DisplayName(value = "Encoder ID", exclude = true)
    private int encoderId;
    @DisplayName(value = "Date Entered", exclude = true)
    private Timestamp dateEntered;
    @DisplayName(value = "Date Modified", exclude = true)
    private byte creditType;
    @DisplayName(value = "Company Code", exclude = true)
    private byte companyCode;
    @DisplayName(value = "Price Type", exclude = true)
    private String priceType; // Lombok generates getter/setter methods, no need for explicit methods
    @DisplayName(value = "Active", exclude = true)
    private boolean isActive;
    @DisplayName(value = "Is VAT", exclude = true)
    private boolean isVAT;
    @DisplayName(value = "Is EWT", exclude = true)
    private boolean isEWT;
    @DisplayName(value = "Other Details", exclude = true)
    private String otherDetails;

    private static final ClusterDAO clusterDAO = new ClusterDAO();
    private Cluster cachedCluster;

    public String getCluster() {
        if (cachedCluster == null) {
            cachedCluster = clusterDAO.getClusterByArea(province, city, brgy);
        }
        return cachedCluster != null ? cachedCluster.getClusterName() : "Cluster Not Found";
    }
}
