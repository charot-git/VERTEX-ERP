package com.vertex.vos;

import com.vertex.vos.Constructors.LineDiscount;
import com.vertex.vos.Utilities.DiscountDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.net.URL;
import java.util.List;


import java.io.IOException;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class DiscountTypeController implements Initializable {
    DiscountDAO discountDAO = new DiscountDAO();

    @FXML
    private HBox addButton;

    @FXML
    private TableView<LineDiscount> discountLink;

    @FXML
    private Label discountType;

    void setDiscountType(String discountName) {
        discountType.setText(discountName);
        discountLink.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        int typeId;
        try {
            typeId = discountDAO.getDiscountTypeIdByName(discountName);
            loadLineDiscountsIntoTableView(typeId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        addButton.setOnMouseClicked(mouseEvent -> {
            try {
                addNewLineDiscountToDiscountType(discountName);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void addNewLineDiscountToDiscountType(String discountName) throws SQLException {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("tableManager.fxml"));
            Parent content = loader.load();

            TableManagerController controller = loader.getController();
            controller.loadLineDiscountTableForLink(discountName);
            controller.setDiscountChangeEventConsumer(discountLink::fireEvent);

            Stage stage = new Stage();
            stage.setTitle("Add line discount to " + discountName);
            stage.setScene(new Scene(content));
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadLineDiscountsIntoTableView(int typeId) {
        try {
            List<LineDiscount> lineDiscounts = discountDAO.getAllLineDiscountsByType(typeId);
            if (!lineDiscounts.isEmpty()) {
                discountLink.getItems().clear();

                discountLink.getColumns().clear();
                setupTableColumns();

                discountLink.getItems().addAll(lineDiscounts);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    private void setupTableColumns() {
        TableColumn<LineDiscount, String> lineDiscountNameCol = new TableColumn<>("Discount Name");
        lineDiscountNameCol.setCellValueFactory(new PropertyValueFactory<>("lineDiscount"));

        TableColumn<LineDiscount, BigDecimal> discountValueCol = new TableColumn<>("Discount Value");
        discountValueCol.setCellValueFactory(new PropertyValueFactory<>("percentage"));

        discountLink.getColumns().addAll(lineDiscountNameCol, discountValueCol);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        discountLink.addEventHandler(DiscountChangeEvent.DISCOUNT_CHANGE_EVENT, event -> {
            // Reload line discounts when the event is fired
            try {
                int typeId = discountDAO.getDiscountTypeIdByName(discountType.getText());
                loadLineDiscountsIntoTableView(typeId);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }
}
