package com.vertex.vos;

import com.vertex.vos.DAO.CollectionDAO;
import com.vertex.vos.Objects.Collection;
import com.vertex.vos.Utilities.DialogUtils;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

public class CollectionListController implements Initializable {

    CollectionDAO collectionDAO = new CollectionDAO();

    ObservableList<Collection> collectionList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        try {
            collectionList.setAll(collectionDAO.getAllCollections());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        collectionTableView.setItems(collectionList);
        collectionNoCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDocNo()));
        salesmanCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getSalesman().getSalesmanName()));
        collectorCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCollectedBy().getUser_fname() + " " + cellData.getValue().getCollectedBy().getUser_lname()));
        amountCol.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getTotalAmount()).asObject());
        collectionDateCol.setCellValueFactory(cellData -> new SimpleObjectProperty<Timestamp>(cellData.getValue().getCollectionDate()));
        postStatusCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getIsPosted() ? "Yes" : "No"));

        addButton.setOnMouseClicked(event -> {
            openNewCollectionForm();
        });

        collectionTableView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Collection collection = collectionTableView.getSelectionModel().getSelectedItem();
                if (collection != null) {
                    openCollectionForm(collection);
                }
            }
        });

    }

    private void openCollectionForm(Collection collection) {
        CompletableFuture.supplyAsync(() -> {
            try {
                return collectionDAO.getCollectionById(collection.getId()); // Fetch in background thread
            } catch (Exception e) {
                e.printStackTrace();
                return null; // Return null if an exception occurs
            }
        }).thenAccept(updatedCollection -> {
            if (updatedCollection == null) {
                Platform.runLater(() -> DialogUtils.showErrorMessage("Error", "Collection not found."));
                return;
            }

            Platform.runLater(() -> {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("CollectionForm.fxml"));
                    Parent root = loader.load();
                    CollectionFormController controller = loader.getController();
                    Stage stage = new Stage();
                    stage.setTitle("Collection Document " + updatedCollection.getDocNo());
                    stage.setMaximized(true);
                    controller.editCollection(stage, updatedCollection, this);

                    stage.setScene(new Scene(root));
                    stage.show();
                } catch (IOException e) {
                    DialogUtils.showErrorMessage("Error", "Unable to open collection form.");
                    e.printStackTrace();
                }
            });
        });
    }


    private void openNewCollectionForm() {
        CompletableFuture.runAsync(() -> {
            try {
                int collectionNumber = collectionDAO.generateCollectionNumber(); // Run in background thread

                Platform.runLater(() -> {
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("CollectionForm.fxml"));
                        Parent root = loader.load();
                        CollectionFormController controller = loader.getController();
                        Stage stage = new Stage();
                        stage.setTitle("New Collection Document " + collectionNumber);
                        stage.setMaximized(true);
                        controller.createCollection(stage, collectionNumber, this);

                        stage.setScene(new Scene(root));
                        stage.show();
                    } catch (IOException e) {
                        DialogUtils.showErrorMessage("Error", "Unable to open collection form.");
                        e.printStackTrace();
                    }
                });
            } catch (SQLException e) {
                Platform.runLater(() -> {
                    DialogUtils.showErrorMessage("Error", "Unable to generate collection number.");
                    e.printStackTrace();
                });
            }
        });
    }


    @FXML
    private TableView<Collection> collectionTableView;
    @FXML
    private Button addButton;

    @FXML
    private TableColumn<Collection, Double> amountCol;

    @FXML
    private TableColumn<Collection, Timestamp> collectionDateCol;

    @FXML
    private TableColumn<Collection, String> collectionNoCol;

    @FXML
    private TableColumn<Collection, String> collectorCol;

    @FXML
    private TextField collectorFilter;

    @FXML
    private TextField numberFilter;

    @FXML
    private TableColumn<Collection, String> postStatusCol;

    @FXML
    private TableColumn<Collection, String> salesmanCol;

    @FXML
    private TextField salesmanFilter;

}
