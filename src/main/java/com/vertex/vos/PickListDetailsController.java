package com.vertex.vos;

import com.vertex.vos.Objects.ProductsInTransact;
import com.vertex.vos.Objects.SalesOrderHeader;
import com.vertex.vos.Utilities.EmployeeDAO;
import com.vertex.vos.Utilities.ProductDAO;
import com.vertex.vos.Utilities.SalesOrderDAO;
import com.vertex.vos.Utilities.WarehouseBrandLinkDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class PickListDetailsController {

    @FXML
    private TableColumn<ProductsInTransact, Boolean> checkedCol;

    @FXML
    private TableColumn<ProductsInTransact, String> descriptionCol;

    @FXML
    private Label employeeName;

    @FXML
    private TableView<ProductsInTransact> pickList;

    @FXML
    private TableColumn<ProductsInTransact, Integer> quantityCol;

    @FXML
    private Label soNumber;

    @FXML
    private TableColumn<ProductsInTransact, String> unitCol;

    EmployeeDAO employeeDAO = new EmployeeDAO();
    SalesOrderDAO salesOrderDAO = new SalesOrderDAO();
    ProductDAO productDAO = new ProductDAO();
    WarehouseBrandLinkDAO brandLinkDAO = new WarehouseBrandLinkDAO();

    public void initData(SalesOrderHeader selectedOrder, int employeeId) {
        soNumber.setText("SO#" + selectedOrder.getOrderId());
        employeeName.setText(employeeDAO.getFullNameById(employeeId));

        ObservableList<ProductsInTransact> orderedProducts = salesOrderDAO.fetchOrderedProducts(selectedOrder.getOrderId());
        ObservableList<Integer> employeeBrands = brandLinkDAO.getLinkedBrands(employeeId);

        ObservableList<ProductsInTransact> filteredProducts = FXCollections.observableArrayList();

        for (ProductsInTransact productsInTransact : orderedProducts) {
            int productBrandId = productDAO.getProductBrandById(productsInTransact.getProductId());
            if (employeeBrands.contains(productBrandId)) {
                filteredProducts.add(productsInTransact);
            }
        }

        pickList.setItems(filteredProducts);

        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        quantityCol.setCellValueFactory(new PropertyValueFactory<>("orderedQuantity"));
        unitCol.setCellValueFactory(new PropertyValueFactory<>("unit"));
    }

    @FXML
    public void initialize() {
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        quantityCol.setCellValueFactory(new PropertyValueFactory<>("orderedQuantity"));
        unitCol.setCellValueFactory(new PropertyValueFactory<>("unit"));
    }
}
