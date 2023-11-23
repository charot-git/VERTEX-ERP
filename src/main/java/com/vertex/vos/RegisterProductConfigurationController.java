package com.vertex.vos;

import com.vertex.vos.Constructors.*;
import com.vertex.vos.Utilities.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

import java.net.URL;
import java.util.ResourceBundle;

public class RegisterProductConfigurationController implements Initializable {
    private Product selectedProduct;
    @FXML
    private Label productHeader;
    @FXML
    private TextField descriptionTextField;
    @FXML
    private Label descriptionErr;
    @FXML
    private Label descriptionLabel;
    @FXML
    private Label unitOfMeasurementLabel;
    @FXML
    private ComboBox unitOfMeasurementComboBox;
    @FXML
    private Label unitOfMeasurementErr;
    @FXML
    private Label countLabel;
    @FXML
    private TextField countTextField;
    @FXML
    private Label countErr;
    @FXML
    private Label priceLabel;
    @FXML
    private TextField priceTextField;
    @FXML
    private Label priceErr;
    @FXML
    private Label quantityLabel;
    @FXML
    private TextField quantityTextField;
    @FXML
    private Label quantityErr;
    @FXML
    private Label barcodeLabel;
    @FXML
    private TextField barcodeTextField;
    @FXML
    private Label barcodeErr;
    @FXML
    private Button confirmButton;
    @FXML
    private Label confirmationLabel;
    @FXML
    private ImageView productPic;
    @FXML
    private HBox changePicButton;
    @FXML
    private TextField estimatedUnitCostTextField;
    @FXML
    private Label estimatedUnitCostErr;
    @FXML
    private TextField estimatedExtendedCostTextField;
    @FXML
    private Label estimatedExtendedCostErr;
    @FXML
    private ComboBox secondaryCategoryComboBox;
    @FXML
    private Label secondaryErr;
    @FXML
    private ComboBox secondarySegmentComboBox;
    @FXML
    private Label secondaryTypeErr;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        TextFieldUtils.setComboBoxBehavior(unitOfMeasurementComboBox);

        UnitDAO unitDAO = new UnitDAO();

        ObservableList<Unit> unitObservableList = unitDAO.getUnitDetails();
        ObservableList<String> unitNames = FXCollections.observableArrayList();
        for (Unit unit : unitObservableList) {
            String unitName = unit.getUnit_name();
            unitNames.add(unitName);
        }
        unitOfMeasurementComboBox.setItems(unitNames);
        ComboBoxFilterUtil.setupComboBoxFilter(unitOfMeasurementComboBox, unitNames);

        //categories
        CategoriesDAO categoriesDAO = new CategoriesDAO();
        ObservableList<Category> categoryObservableList = categoriesDAO.getCategoryDetails();
        ObservableList<String> categoryNames = FXCollections.observableArrayList();
        for (Category category : categoryObservableList) {
            int category_id = category.getCategory_id();
            String category_name = category.getCategory_name();
            categoryNames.add(category_name);
        }
        secondaryCategoryComboBox.setItems(categoryNames);
        ComboBoxFilterUtil.setupComboBoxFilter(secondaryCategoryComboBox, categoryNames);

        //segments
        SegmentDAO segmentDAO = new SegmentDAO();
        ObservableList<Segment> segmentObservableList = segmentDAO.getSegmentDetails();
        ObservableList<String> segmentNames = FXCollections.observableArrayList();
        for (Segment segment : segmentObservableList) {
            int segment_id = segment.getSegment_id();
            String segment_name = segment.getSegment_name();
            segmentNames.add(segment_name);
        }
        secondarySegmentComboBox.setItems(segmentNames);
        ComboBoxFilterUtil.setupComboBoxFilter(secondarySegmentComboBox, segmentNames);


    }

    private void registerProductDetails(String product) {
        ProductDAO productDAO = new ProductDAO();
        ProductInventory productInventory = new ProductInventory();

        String unitOfMeasurement = String.valueOf(unitOfMeasurementComboBox.getSelectionModel().getSelectedItem());

        UnitDAO unitDAO = new UnitDAO();
        int unit = unitDAO.getUnitIdByName(unitOfMeasurement);

        CategoriesDAO categoriesDAO = new CategoriesDAO();
        int category = categoriesDAO.getCategoryIdByName(String.valueOf(secondaryCategoryComboBox.getSelectionModel().getSelectedItem()));

        SegmentDAO segmentDAO = new SegmentDAO();
        int segment = segmentDAO.getSegmentIdByName(String.valueOf(secondarySegmentComboBox.getSelectionModel().getSelectedItem()));

        productInventory.setProduct_id(productDAO.getProductIdByName(product));
        productInventory.setDescription(descriptionTextField.getText());
        productInventory.setUnit_of_measurement(unit);
        productInventory.setUnit_of_measurement_count(Integer.parseInt(countTextField.getText()));
        productInventory.setPrice(Double.valueOf(priceTextField.getText()));
        productInventory.setBarcode(barcodeTextField.getText());
        productInventory.setSecondary_category(category);
        productInventory.setSecondary_segment(segment);
        productInventory.setEstimated_unit_cost(Double.valueOf(estimatedUnitCostTextField.getText()));
        productInventory.setEstimated_extended_cost(Double.valueOf(estimatedExtendedCostTextField.getText()));

        boolean registerConfig = productDAO.addProductConfiguration(productInventory);

        if (registerConfig) {
            confirmButton.getScene().getWindow().hide(); // This will close the window
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Error occurred!"); // Set the content of the error message
            alert.show();
        }
    }

    public void initData(String product) {
        productHeader.setText(product);

        confirmButton.setOnMouseClicked(mouseEvent -> registerProductDetails(product));
    }
}
