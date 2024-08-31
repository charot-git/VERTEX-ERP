package com.vertex.vos;

import com.vertex.vos.DAO.ClusterDAO;
import com.vertex.vos.Objects.AreaPerCluster;
import com.vertex.vos.Objects.Cluster;
import com.vertex.vos.Utilities.LocationComboBoxUtil;
import com.vertex.vos.Utilities.LocationUtils;
import com.vertex.vos.Utilities.TextFieldUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

import java.util.List;

public class ClusterRegistrationController {

    public TextField minimumAmountTextField;
    public TableView<AreaPerCluster> areaTableView;
    @FXML
    private Button addArea;

    @FXML
    private Button addCluster;

    @FXML
    private ComboBox<String> baranggayComboBox;

    @FXML
    private TableColumn<AreaPerCluster, String> brgyCol;

    @FXML
    private TableColumn<AreaPerCluster, String> cityCol;

    @FXML
    private ComboBox<String> cityComboBox;

    @FXML
    private ListView<Cluster> clusterListView;

    @FXML
    private TextField clusterTextField;

    @FXML
    private HBox header;

    @FXML
    private TableColumn<AreaPerCluster, String> provinceCol;

    @FXML
    private ComboBox<String> provinceComboBox;

    private ClusterDAO clusterDAO;
    private ObservableList<Cluster> clusterList;
    private ObservableList<AreaPerCluster> areaList;

    public void initialize() {
        clusterDAO = new ClusterDAO();
        clusterList = FXCollections.observableArrayList();
        areaList = FXCollections.observableArrayList();

        LocationComboBoxUtil locationUtil = new LocationComboBoxUtil(provinceComboBox, cityComboBox, baranggayComboBox);
        locationUtil.initializeComboBoxes();

        loadClusters();

        setupContextMenu();


        TextFieldUtils.addDoubleInputRestriction(minimumAmountTextField);

        clusterListView.setItems(clusterList);

        clusterListView.setCellFactory(lv -> new ListCell<Cluster>() {
            @Override
            protected void updateItem(Cluster item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getClusterName()); // Assuming getClusterName() returns the name of the cluster
                }
            }
        });

        clusterListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        clusterListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                clusterTextField.setText(newValue.getClusterName());
                minimumAmountTextField.setText(String.valueOf(newValue.getMinimumAmount()));
                addCluster.setText("Update");
                addCluster.setOnMouseClicked(mouseEvent -> onUpdateClusterClicked(newValue));
                loadAreas(newValue.getId());
            }
        });


        clusterListView.setOnMouseClicked(this::onClusterSelected);

        addArea.setOnMouseClicked(mouseEvent -> onAddAreaClicked());
        addCluster.setOnMouseClicked(mouseEvent -> onAddClusterClicked());
    }

    private void loadAreas(int id) {
        areaList.clear();
        List<AreaPerCluster> areas = clusterDAO.getAreasByClusterId(id);
        areaList.addAll(areas);

        brgyCol.setCellValueFactory(new PropertyValueFactory<>("baranggay"));
        cityCol.setCellValueFactory(new PropertyValueFactory<>("city"));
        provinceCol.setCellValueFactory(new PropertyValueFactory<>("province"));
        areaTableView.setItems(areaList);
    }

    private void setupContextMenu() {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem deleteItem = new MenuItem("Delete");

        // Handle the delete action
        deleteItem.setOnAction(event -> {
            AreaPerCluster selectedArea = areaTableView.getSelectionModel().getSelectedItem();
            if (selectedArea != null) {
                boolean confirmation = showConfirmation("Are you sure you want to delete this area?");
                if (confirmation) {
                    if (clusterDAO.deleteAreaFromCluster(selectedArea.getId())) {
                        areaList.remove(selectedArea);
                    } else {
                        showError("Failed to delete area.");
                    }
                }
            }
        });

        contextMenu.getItems().add(deleteItem);

        // Set the context menu to the areaTableView
        areaTableView.setRowFactory(tv -> {
            TableRow<AreaPerCluster> row = new TableRow<>();
            row.setOnContextMenuRequested(event -> {
                if (!row.isEmpty()) {
                    areaTableView.getSelectionModel().select(row.getIndex());
                    contextMenu.show(row, event.getScreenX(), event.getScreenY());
                }
            });
            return row;
        });
    }

    private boolean showConfirmation(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setContentText(message);
        alert.showAndWait();
        return alert.getResult() == ButtonType.OK;
    }

    private void onUpdateClusterClicked(Cluster newValue) {
        if (newValue != null) {
            Cluster cluster = new Cluster();
            cluster.setId(newValue.getId());
            cluster.setClusterName(clusterTextField.getText().trim());
            cluster.setMinimumAmount(Double.parseDouble(minimumAmountTextField.getText().trim()));
            if (clusterDAO.updateCluster(cluster)) {
                cluster.setClusterName(clusterTextField.getText().trim());
                cluster.setMinimumAmount(Double.parseDouble(minimumAmountTextField.getText().trim()));
                loadClusters();
                clusterListView.refresh();
                clusterTextField.clear();
                minimumAmountTextField.clear();
                addCluster.setText("Add");
            } else {
                showError("Failed to update cluster.");
            }
        }
    }

    public void onAddClusterClicked() {
        String clusterName = clusterTextField.getText().trim();
        if (!clusterName.isEmpty()) {
            Cluster cluster = new Cluster();
            cluster.setClusterName(clusterName);
            cluster.setMinimumAmount(Double.parseDouble(minimumAmountTextField.getText().trim()));
            if (clusterDAO.addCluster(cluster)) {
                clusterList.add(cluster);
                clusterTextField.clear();
                minimumAmountTextField.clear();
                loadClusters();
            } else {
                showError("Failed to add cluster.");
            }
        } else {
            showError("Cluster name cannot be empty.");
        }
    }

    public void onAddAreaClicked() {
        Cluster selectedCluster = clusterListView.getSelectionModel().getSelectedItem();
        if (selectedCluster != null) {
            String province = provinceComboBox.getValue();
            String city = cityComboBox.getValue();
            String baranggay = baranggayComboBox.getValue();

            if (province != null && city != null) {
                AreaPerCluster area = new AreaPerCluster();
                area.setClusterId(selectedCluster.getId());
                area.setProvince(province);
                area.setCity(city);
                area.setBaranggay(baranggay);

                if (clusterDAO.addAreaToCluster(area)) {
                    areaList.add(area);
                    provinceComboBox.getSelectionModel().clearSelection();
                    cityComboBox.getSelectionModel().clearSelection();
                    baranggayComboBox.getSelectionModel().clearSelection();
                } else {
                    showError("Failed to add area.");
                }
            } else {
                showError("Please select province, city.");
            }
        } else {
            showError("Please select a cluster first.");
        }
    }

    private void loadClusters() {
        List<Cluster> clusters = clusterDAO.getAllClusters();
        clusterList.setAll(clusters);
    }

    private void onClusterSelected(MouseEvent event) {
        Cluster selectedCluster = clusterListView.getSelectionModel().getSelectedItem();
        if (selectedCluster != null) {
            List<AreaPerCluster> areas = clusterDAO.getAreasByClusterId(selectedCluster.getId());
            areaList.setAll(areas);
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(message);
        alert.show();
    }
}
