package com.dp.notary.blockchain.ui;

import com.dp.notary.blockchain.App;
import com.dp.notary.blockchain.api.client.LeaderClient;
import com.dp.notary.blockchain.api.client.ReplicaClient;
import com.dp.notary.blockchain.auth.Role;
import com.dp.notary.blockchain.auth.SessionService;
import com.dp.notary.blockchain.behavior.RoleBehavior;
import com.dp.notary.blockchain.blockchain.BlockchainService;
import com.dp.notary.blockchain.blockchain.model.TransactionStatus;
import com.dp.notary.blockchain.config.NotaryProperties;
import com.dp.notary.blockchain.owner.OwnerService;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.*;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

import static org.apache.logging.log4j.util.Strings.trimToNull;

@Component
public class DashboardController {

    /* =======================
       FXML
       ======================= */

    @FXML private Label companyNameLabel;

    @FXML private TextField ownerFilterField;
    @FXML private TableView<OwnerSharesVM> sharesTable;
    @FXML private TableColumn<OwnerSharesVM, String> ownerCol;
    @FXML private TableColumn<OwnerSharesVM, String> sharesCol;

    private String ownerFilter;

    @FXML private VBox statsBox;

    /* =======================
       Data
       ======================= */

    private final ObservableList<OwnerSharesVM> masterData = FXCollections.observableArrayList();

    private BlockchainService blockchainService;
    private final SessionService sessionService;
    private OwnerService ownerService;

//    private FilteredList<OwnerSharesVM> filteredData;


    DashboardController(BlockchainService blockchainService, SessionService sessionService, OwnerService ownerService){
        this.blockchainService = blockchainService;
        this.sessionService = sessionService;
        this.ownerService = ownerService;
    }

    /* =======================
       Init
       ======================= */

    @FXML
    public void initialize() {
        setupTable();
        setDataAndButtons();
    }

    public void setDataAndButtons() {
        setCompany(new DashboardController.CompanyVM("H&P.Co"));

        getSharesData();

        if (sessionService.validateRole(Role.LEADER)) {
            configureForLeader(
                    new LeaderStatsVM(
                            blockchainService.totalTransactions(TransactionStatus.APPROVED, null, null, null, null),
                            blockchainService.totalTransactions(TransactionStatus.SUBMITTED, null, null, null, null),
                            blockchainService.totalTransactions(TransactionStatus.DRAFT, sessionService.getName(), null, null, null)
                    )
            );
        } else {
            String username = sessionService.getName();
            configureForReplica(
                    new ReplicaStatsVM(
                            blockchainService.totalTransactions(TransactionStatus.APPROVED, username, null, null, null),
                            blockchainService.totalTransactions(TransactionStatus.SUBMITTED, username, null, null, null),
                            blockchainService.totalTransactions(TransactionStatus.DRAFT, username, null, null, null),
                            blockchainService.totalTransactions(TransactionStatus.DECLINED, username, null, null, null)
                    )
            );
        }
    }

    private void getSharesData() {
        if (!sessionService.isAuthenticated()){
            App.get().showLogin();
            return;
        }

        setSharesData(ownerService.getOwnersShares(ownerFilter).stream()
                .map(o -> new DashboardController.OwnerSharesVM(o.getName_surname(), o.getShares())).toList());
    }

    private void setupTable() {
        ownerCol.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().owner()));

        sharesCol.setCellValueFactory(c ->
                new SimpleStringProperty(String.valueOf(c.getValue().shares())));

        ownerCol.setSortable(true);
        sharesCol.setSortable(true);

        sharesTable.setItems(masterData);
    }

    @FXML
    private void onClearFilters() {
        if (!sessionService.isAuthenticated()){
            App.get().showLogin();
            return;
        }

        ownerFilterField.clear();

        onApplyFilters();
    }

    @FXML
    private void onApplyFilters() {
        if (!sessionService.isAuthenticated()){
            App.get().showLogin();
            return;
        }

        ownerFilter = trimToNull(ownerFilterField.getText());

        getSharesData();
    }

    /* =======================
       Public API
       ======================= */

    public void setCompany(CompanyVM company) {
        companyNameLabel.setText(company.name());
    }

    public void setSharesData(java.util.List<OwnerSharesVM> rows) {
        masterData.setAll(rows);
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
        textLabel.setWrapText(true);

        VBox box = new VBox(6, valueLabel, textLabel);
        box.setAlignment(javafx.geometry.Pos.CENTER);

        return box;
    }

    /* =======================
       View models
       ======================= */

    public record OwnerSharesVM(String owner, BigDecimal shares) {}

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

