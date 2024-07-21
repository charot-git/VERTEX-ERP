package com.vertex.vos;

import com.vertex.vos.Objects.User;
import com.vertex.vos.Utilities.BrandDAO;
import com.vertex.vos.Utilities.EmployeeDAO;
import com.vertex.vos.Utilities.ImageCircle;
import com.vertex.vos.Utilities.WarehouseBrandLinkDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class AssignBrandToWarehouseMenController implements Initializable {

    @FXML
    private ListView<String> brandList;

    @FXML
    private ImageView employeeImage;

    @FXML
    private Label employeeName;

    @FXML
    private HBox header;

    @FXML
    private ListView<String> linkedBrandList;

    BrandDAO brandDAO = new BrandDAO();
    EmployeeDAO employeeDAO = new EmployeeDAO();
    WarehouseBrandLinkDAO warehouseBrandLinkDAO = new WarehouseBrandLinkDAO();

    private int currentEmployeeId;

    public void initData(int employeeId) {
        this.currentEmployeeId = employeeId;
        User user = employeeDAO.getUserById(employeeId);
        employeeName.setText(user.getUser_fname() + " " + user.getUser_lname());
        String url = user.getUser_image();
        ImageCircle.cicular(employeeImage);
        if (url == null) {
            Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/profile.png")));
            employeeImage.setImage(image);
        } else {
            Image userImage = new Image(url);
            employeeImage.setImage(userImage);
        }
        loadLinkedBrands(employeeId);
    }
    ObservableList<String> brandNames = brandDAO.getBrandNames();
    ObservableList<String> linkedBrandNames = FXCollections.observableArrayList();
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        brandList.setItems(brandNames);
        linkedBrandList.setItems(linkedBrandNames);

        // Add double-click event handlers
        brandList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                addToLinkedBrandList();
            }
        });

        linkedBrandList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                removeFromLinkedBrandList();
            }
        });
    }

    private void addToLinkedBrandList() {
        String selectedBrand = brandList.getSelectionModel().getSelectedItem();
        if (selectedBrand != null && !linkedBrandNames.contains(selectedBrand)) {
            int brandId = brandDAO.getBrandIdByName(selectedBrand);
            boolean success = warehouseBrandLinkDAO.linkBrandToWarehouseman(currentEmployeeId, brandId);
            if (success) {
                linkedBrandNames.add(selectedBrand);
            }
        }
    }

    private void removeFromLinkedBrandList() {
        String selectedBrand = linkedBrandList.getSelectionModel().getSelectedItem();
        if (selectedBrand != null) {
            int brandId = brandDAO.getBrandIdByName(selectedBrand);
            boolean success = warehouseBrandLinkDAO.unlinkBrandFromWarehouseman(currentEmployeeId, brandId);
            if (success) {
                linkedBrandNames.remove(selectedBrand);
            }
        }
    }

    private void loadLinkedBrands(int employeeId) {
        ObservableList<Integer> brandIds = warehouseBrandLinkDAO.getLinkedBrands(employeeId);
        for (int brandId : brandIds) {
            String brandName = brandDAO.getBrandNameById(brandId);
            if (brandName != null) {
                linkedBrandNames.add(brandName);
            }
        }
    }
}
