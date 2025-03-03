package com.vertex.vos;

import com.vertex.vos.DAO.PhysicalInventoryDAO;
import com.vertex.vos.DAO.PhysicalInventoryDetailsDAO;
import com.vertex.vos.Objects.PhysicalInventory;
import com.vertex.vos.Objects.PhysicalInventoryDetails;
import com.vertex.vos.Utilities.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.converter.IntegerStringConverter;

import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.Comparator;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class OffsettingFormController implements Initializable {

    public BorderPane borderPane;
    public Label docNo;
    public Label auditedBy;
    public DatePicker dateAuditedPicker;
    public DatePicker dateFromPicker;
    public DatePicker dateToPicker;
    public Label totalShortageLabel;
    public Label findingLabel;
    public Label skuIssuancePercent;
    public Label pcsServingPercent;
    public Label amountCollectionPercent;
    public Label overallAuditPerformancePercent;
    public Label excellenceLabel;
    public Button calculateShortage;
    public Button commitOffset;
    public Label shortAmount;
    public Label overAmount;
    public TableView<PhysicalInventoryDetails> shortTable;
    public TableColumn<PhysicalInventoryDetails, Integer> matchNoColShort;
    public TableColumn<PhysicalInventoryDetails, String> brandColShort;
    public TableColumn<PhysicalInventoryDetails, String> productNameColShort;
    public TableColumn<PhysicalInventoryDetails, String> productUnitColShort;
    public TableColumn<PhysicalInventoryDetails, Double> productPriceColShort;
    public TableColumn<PhysicalInventoryDetails, Integer> systemCountColShort;
    public TableColumn<PhysicalInventoryDetails, Integer> physicalCountColShort;
    public TableColumn<PhysicalInventoryDetails, Integer> varianceCountColShort;
    public TableColumn<PhysicalInventoryDetails, Double> amountColShort;
    public TableView<PhysicalInventoryDetails> overTable;
    public TableColumn<PhysicalInventoryDetails, Integer> matchNoColOver;
    public TableColumn<PhysicalInventoryDetails, String> brandColOver;
    public TableColumn<PhysicalInventoryDetails, String> productNameColOver;
    public TableColumn<PhysicalInventoryDetails, String> productUnitColOver;
    public TableColumn<PhysicalInventoryDetails, Double> productPriceColOver;
    public TableColumn<PhysicalInventoryDetails, Integer> systemCountColOver;
    public TableColumn<PhysicalInventoryDetails, Integer> physicalCountColOver;
    public TableColumn<PhysicalInventoryDetails, Integer> varianceCountColOver;
    public TableColumn<PhysicalInventoryDetails, Double> amountColOver;
    public Label branchName;
    public Label chargeTo;
    @FXML
    private TableView<PhysicalInventoryDetails> offsettingTable;
    @FXML
    private TableColumn<PhysicalInventoryDetails, Integer> matchNoCol;
    @FXML
    private TableColumn<PhysicalInventoryDetails, String> brandCol, findingsCol, productNameCol, productUnitCol;
    @FXML
    private TableColumn<PhysicalInventoryDetails, Double> productPriceCol, amountCol;
    @FXML
    private TableColumn<PhysicalInventoryDetails, Integer> physicalCountCol, systemCountCol, varianceCountCol;

    private final PhysicalInventoryDetailsDAO physicalInventoryDetailsDAO = new PhysicalInventoryDetailsDAO();
    private final MatchColorManager matchColorManager = new MatchColorManager();
    private final ObservableList<PhysicalInventoryDetails> physicalInventoryDetails = FXCollections.observableArrayList();

    private final ObservableList<PhysicalInventoryDetails> shortList = FXCollections.observableArrayList();
    private final ObservableList<PhysicalInventoryDetails> overList = FXCollections.observableArrayList();

    private PhysicalInventory physicalInventory;

    EmployeeDAO employeeDAO = new EmployeeDAO();

    PhysicalInventoryDAO physicalInventoryDAO = new PhysicalInventoryDAO();

    public void loadOffsettingForm(PhysicalInventory selectedInventory) {
        this.physicalInventory = selectedInventory;

        // Show loading placeholders
        Label loadingLabel = new Label("Loading...");
        ProgressIndicator progressIndicator = new ProgressIndicator();
        VBox loadingBox = new VBox(progressIndicator, loadingLabel);
        loadingBox.setAlignment(Pos.CENTER);

        offsettingTable.setPlaceholder(loadingBox);
        overTable.setPlaceholder(loadingBox);
        shortTable.setPlaceholder(loadingBox);

        // Run database operations asynchronously
        CompletableFuture<ObservableList<PhysicalInventoryDetails>> detailsFuture =
                CompletableFuture.supplyAsync(() -> physicalInventoryDetailsDAO.getPhysicalInventoryDetailsWithVariance(selectedInventory.getId()));

        CompletableFuture<String> auditedByFuture =
                CompletableFuture.supplyAsync(() -> employeeDAO.getFullNameById(selectedInventory.getEncoderId()));

        CompletableFuture<Timestamp> dateFromFuture =
                CompletableFuture.supplyAsync(() -> physicalInventoryDAO.getLastInventoryDateFromCutOff(selectedInventory));

        // When all tasks are complete, update the UI
        CompletableFuture.allOf(detailsFuture, auditedByFuture, dateFromFuture)
                .thenRun(() -> Platform.runLater(() -> {
                    try {
                        physicalInventoryDetails.setAll(detailsFuture.get());
                        auditedBy.setText(auditedByFuture.get());

                        docNo.setText(selectedInventory.getPhNo());
                        dateAuditedPicker.setValue(selectedInventory.getDateEncoded().toLocalDateTime().toLocalDate());
                        dateToPicker.setValue(selectedInventory.getCutOffDate().toLocalDateTime().toLocalDate());
                        branchName.setText(selectedInventory.getBranch().getBranchDescription());
                        chargeTo.setText(selectedInventory.getBranch().getBranchHeadName());

                        Timestamp dateFrom = dateFromFuture.get();
                        if (dateFrom != null) {
                            dateFromPicker.setValue(dateFrom.toLocalDateTime().toLocalDate());
                        }

                        for (PhysicalInventoryDetails details : physicalInventoryDetails) {
                            if (details.getOffsetMatch() != 0) {
                                updateShortAndOverAmounts();
                                calculatePhysicalInventoryShortage();
                            }
                        }

                        if (selectedInventory.isCommitted()) {
                            borderPane.setDisable(true);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        DialogUtils.showErrorMessage("Error", "Failed to load offsetting form.");
                    } finally {
                        // Restore placeholders after loading
                        offsettingTable.setPlaceholder(new Label("No data found"));
                        overTable.setPlaceholder(new Label("No data found"));
                        shortTable.setPlaceholder(new Label("No data found"));
                    }
                }));
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        TableViewFormatter.formatTableView(offsettingTable);
        initializeOffsettingColumn();
        initializeShortColumns();
        initializeOverColumn();

        offsettingTable.setItems(getSortedList());
        overTable.setItems(overList);
        shortTable.setItems(shortList);

        calculateShortage.setOnAction(event -> {
            calculatePhysicalInventoryShortage();

        });

        commitOffset.setOnAction(event -> {
            boolean success = physicalInventoryDAO.commitOffset(offsettingTable.getItems());
            if (success) {
                DialogUtils.showCompletionDialog("Success", "Offset successfully committed.");
                try {
                    NoticeOfDecisionGenerator.generateNoticeOfDecision(physicalInventory, chargeTo.getText(), auditedBy.getText(), physicalInventoryDetails, totalShortageLabel.getText(), findingLabel.getText(), dateFromPicker.getValue(), dateToPicker.getValue());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                DialogUtils.showErrorMessage("Error", "Offset not committed.");
            }
        });


        physicalInventoryDetails.addListener((ListChangeListener<PhysicalInventoryDetails>) change -> updateShortAndOverLists());

    }

    private void updateShortAndOverLists() {
        shortList.setAll(
                physicalInventoryDetails.stream()
                        .filter(details -> details.getOffsetMatch() != 0 && details.getVariance() < 0)
                        .collect(Collectors.toList())
        );

        overList.setAll(
                physicalInventoryDetails.stream()
                        .filter(details -> details.getOffsetMatch() != 0 && details.getVariance() > 0)
                        .collect(Collectors.toList())
        );

        updateShortAndOverAmounts();
    }


    private void initializeOverColumn() {
        matchNoColOver.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getOffsetMatch()).asObject());
        brandColOver.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduct().getProductBrandString()));
        productNameColOver.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduct().getProductName()));
        productUnitColOver.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduct().getUnitOfMeasurementString()));
        productPriceColOver.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getUnitPrice()).asObject());
        systemCountColOver.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getSystemCount()).asObject());
        physicalCountColOver.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getPhysicalCount()).asObject());
        varianceCountColOver.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getVariance()).asObject());
        amountColOver.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getAmount()).asObject());
    }

    private void initializeShortColumns() {
        matchNoColShort.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getOffsetMatch()).asObject());
        brandColShort.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduct().getProductBrandString()));
        productNameColShort.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduct().getProductName()));
        productUnitColShort.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduct().getUnitOfMeasurementString()));
        productPriceColShort.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getUnitPrice()).asObject());
        systemCountColShort.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getSystemCount()).asObject());
        physicalCountColShort.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getPhysicalCount()).asObject());
        varianceCountColShort.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getVariance()).asObject());
        amountColShort.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getAmount()).asObject());
    }

    private void calculatePhysicalInventoryShortage() {
        double shortage = shortAmountTotal();
        double overage = overAmountTotal();
        double balance = shortage + overage;

        if (balance < 0) {
            totalShortageLabel.setTextFill(Color.RED);
            findingLabel.setTextFill(Color.RED);
            findingLabel.setText("Shortage");
        } else if (balance > 0) {
            totalShortageLabel.setTextFill(Color.ORANGE);
            findingLabel.setTextFill(Color.ORANGE);
            findingLabel.setText("Overage");
        } else {
            totalShortageLabel.setTextFill(Color.GREEN);
            findingLabel.setTextFill(Color.GREEN);
            findingLabel.setText("Balanced");
        }

        totalShortageLabel.setText(String.format("%.2f", balance));
    }

    private void initializeOffsettingColumn() {
        brandCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduct().getProductBrandString()));
        productNameCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduct().getProductName()));
        productUnitCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduct().getUnitOfMeasurementString()));
        productPriceCol.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getUnitPrice()).asObject());

        physicalCountCol.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getPhysicalCount()).asObject());
        systemCountCol.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getSystemCount()).asObject());
        varianceCountCol.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getVariance()).asObject());

        findingsCol.setCellValueFactory(cellData -> new SimpleStringProperty(getVarianceStatus(cellData.getValue())));
        findingsCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    PhysicalInventoryDetails details = getTableRow().getItem();
                    if (details != null) {
                        setStyle(getVarianceStyle(details.getVariance()));
                    }
                }
            }
        });

        matchNoCol.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getOffsetMatch()).asObject());
        matchNoCol.setEditable(true);

        // ✅ Ensure column remains editable while updating colors
        matchNoCol.setCellFactory(column -> new TextFieldTableCell<>(new IntegerStringConverter()) {
            @Override
            public void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.valueOf(item));
                    String color = matchColorManager.getColorForMatch(item);
                    setStyle("-fx-background-color: " + color + "; -fx-text-fill: black;");
                    TableRow<?> row = getTableRow();
                    if (row != null) {
                        row.setStyle("-fx-background-color: " + color + ";");
                    }
                }
            }
        });

        // ✅ Ensure proper color and refresh after edit
        matchNoCol.setOnEditCommit(event -> {
            PhysicalInventoryDetails details = event.getRowValue();
            if (details != null) {
                int newMatchNo = event.getNewValue();
                details.setOffsetMatch(newMatchNo);

                // Ensure color is generated
                matchColorManager.getColorForMatch(newMatchNo);
                updateShortAndOverLists();

                // ✅ Refresh table to update colors
                offsettingTable.refresh();
                updateShortAndOverAmounts();
                offsettingTable.requestFocus();
            }
        });
        amountCol.setCellValueFactory(physicalInventoryDetailsDoubleCellDataFeatures -> new SimpleObjectProperty<>(physicalInventoryDetailsDoubleCellDataFeatures.getValue().getDifferenceCost()));

        offsettingTable.setEditable(true);

    }

    private void updateShortAndOverAmounts() {
        shortAmount.setText(String.format("%.2f", shortAmountTotal()));
        overAmount.setText(String.format("%.2f", overAmountTotal()));
    }

    private double shortAmountTotal() {
        return physicalInventoryDetails.stream()
                .filter(details -> details.getOffsetMatch() != 0)
                .filter(details -> details.getVariance() < 0)
                .mapToDouble(PhysicalInventoryDetails::getDifferenceCost)
                .sum();
    }

    private double overAmountTotal() {
        return physicalInventoryDetails.stream().filter(details -> details.getOffsetMatch() != 0).filter(details -> details.getVariance() > 0).mapToDouble(PhysicalInventoryDetails::getDifferenceCost).sum();
    }


    private SortedList<PhysicalInventoryDetails> getSortedList() {
        SortedList<PhysicalInventoryDetails> sortedList = new SortedList<>(physicalInventoryDetails);
        sortedList.setComparator(
                Comparator.comparing((PhysicalInventoryDetails d) -> d.getProduct().getProductBrandString(), String::compareToIgnoreCase)
                        .thenComparing(d -> getVarianceStatus(d), Comparator.reverseOrder()));
        return sortedList;
    }

    private String getVarianceStatus(PhysicalInventoryDetails details) {
        if (details.getVariance() < 0) return "Short";
        if (details.getVariance() > 0) return "Over";
        return "Balance";
    }

    private String getVarianceStyle(double variance) {
        if (variance < 0) return "-fx-background-color: #D11141 ; -fx-text-fill: white;"; // Short (negative variance)
        if (variance > 0) return "-fx-background-color: #FFC425; -fx-text-fill: black;"; // Over (positive variance)
        return "-fx-background-color: #00B159 ; -fx-text-fill: white;"; // Balance (zero variance)
    }
}
