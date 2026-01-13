package com.dp.notary.blockchain.ui;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.*;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class DashboardController {

    /* =======================
       FXML
       ======================= */

    @FXML private Label companyNameLabel;

    @FXML private TextField ownerFilterField;
    @FXML private TableView<OwnerSharesVM> sharesTable;
    @FXML private TableColumn<OwnerSharesVM, String> ownerCol;
    @FXML private TableColumn<OwnerSharesVM, Number> sharesCol;

    @FXML private Label totalSharesLabel;
    @FXML private VBox statsBox;

    /* =======================
       Data
       ======================= */

    private final ObservableList<OwnerSharesVM> masterData =
            FXCollections.observableArrayList();

    private FilteredList<OwnerSharesVM> filteredData;

    /* =======================
       Init
       ======================= */

    @FXML
    public void initialize() {
        setupTable();
        setupFiltering();
    }

    private void setupTable() {
        ownerCol.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().owner()));

        sharesCol.setCellValueFactory(c ->
                new SimpleIntegerProperty(c.getValue().shares()));

        // built-in sorting
        ownerCol.setSortable(true);
        sharesCol.setSortable(true);

        filteredData = new FilteredList<>(masterData, p -> true);
        sharesTable.setItems(filteredData);
    }

    private void setupFiltering() {
        ownerFilterField.textProperty().addListener((obs, oldV, newV) -> {
            String filter = newV == null ? "" : newV.toLowerCase();

            filteredData.setPredicate(vm ->
                    vm.owner().toLowerCase().contains(filter)
            );
        });
    }

    /* =======================
       Public API
       ======================= */

    public void setCompany(CompanyVM company) {
        // TODO взять название компании из Company (domain / dto)
        companyNameLabel.setText(company.name());
    }

    public void setSharesData(
            java.util.List<OwnerSharesVM> rows,
            int totalShares
    ) {
        // TODO данные из backend / ledger state
        masterData.setAll(rows);
        totalSharesLabel.setText("Total shares: " + totalShares);
    }

    public void configureForLeader(LeaderStatsVM stats) {
        statsBox.getChildren().setAll(
                statBlock(stats.totalTransactions(), "Transactions"),
                statBlock(stats.pendingTransactions(), "Pending transactions"),
                statBlock(stats.drafts(), "Drafts")
        );
    }

    public void configureForReplica(ReplicaStatsVM stats) {
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(20);

        grid.add(statBlock(stats.totalTransactions(), "Transactions"), 0, 0);
        grid.add(statBlock(stats.submitted(), "Submitted transactions"), 1, 0);
        grid.add(statBlock(stats.drafts(), "Drafts"), 0, 1);
        grid.add(statBlock(stats.declined(), "Declined transactions"), 1, 1);

        statsBox.getChildren().setAll(grid);
    }

    /* =======================
       UI helpers
       ======================= */

    private VBox statBlock(int value, String label) {
        Label valueLabel = new Label(String.valueOf(value));
        valueLabel.getStyleClass().add("stat-value");

        Label textLabel = new Label(label);
        textLabel.getStyleClass().add("stat-label");

        return new VBox(4, valueLabel, textLabel);
    }

    /* =======================
       View models
       ======================= */

    public record OwnerSharesVM(String owner, int shares) {}

    public record CompanyVM(String name) {}

    public record LeaderStatsVM(
            int totalTransactions,
            int pendingTransactions,
            int drafts
    ) {}

    public record ReplicaStatsVM(
            int totalTransactions,
            int submitted,
            int drafts,
            int declined
    ) {}
}

