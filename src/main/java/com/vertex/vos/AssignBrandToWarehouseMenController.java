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
import java.util.Comparator;
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

    private BrandDAO brandDAO = new BrandDAO();
    private EmployeeDAO employeeDAO = new EmployeeDAO();
    private WarehouseBrandLinkDAO warehouseBrandLinkDAO = new WarehouseBrandLinkDAO();

    private User currentEmployee;

    private ObservableList<String> brandNames;
    private ObservableList<String> linkedBrandNames = FXCollections.observableArrayList();

    public void initData(User employee) {
        this.currentEmployee = employee;
        employeeName.setText(currentEmployee.getUser_fname() + " " + currentEmployee.getUser_lname());

        String url = currentEmployee.getUser_image();
        ImageCircle.circular(employeeImage);
        if (url == null) {
            Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/profile.png")));
            employeeImage.setImage(image);
        } else {
            Image userImage = new Image(url);
            employeeImage.setImage(userImage);
        }

        loadLinkedBrands();

        // Initialize brandNames after loading linked brands
        brandNames = brandDAO.getBrandNames();
        // Remove linked brands from the available brand list
        brandNames.removeAll(linkedBrandNames);
        // Optionally sort the available brands
        brandNames.sort(Comparator.naturalOrder());

        // Update the ListView with the available brands
        brandList.setItems(brandNames);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
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
            boolean success = warehouseBrandLinkDAO.linkBrandToWarehouseman(currentEmployee.getUser_id(), brandId);
            if (success) {
                linkedBrandNames.add(selectedBrand);
                brandNames.remove(selectedBrand); // Remove from available brands
            }
        }
    }

    private void removeFromLinkedBrandList() {
        String selectedBrand = linkedBrandList.getSelectionModel().getSelectedItem();
        if (selectedBrand != null) {
            int brandId = brandDAO.getBrandIdByName(selectedBrand);
            boolean success = warehouseBrandLinkDAO.unlinkBrandFromWarehouseman(currentEmployee.getUser_id(), brandId);
            if (success) {
                linkedBrandNames.remove(selectedBrand);
                brandNames.add(selectedBrand); // Add back to available brands
                brandNames.sort(Comparator.naturalOrder()); // Re-sort the available brands
            }
        }
    }

    private void loadLinkedBrands() {
        ObservableList<Integer> brandIds = warehouseBrandLinkDAO.getLinkedBrands(currentEmployee.getUser_id());
        for (int brandId : brandIds) {
            String brandName = brandDAO.getBrandNameById(brandId);
            if (brandName != null) {
                linkedBrandNames.add(brandName);
            }
        }
    }
}
