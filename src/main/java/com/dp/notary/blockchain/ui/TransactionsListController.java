package com.dp.notary.blockchain.ui;

import com.dp.notary.blockchain.App;
import com.dp.notary.blockchain.api.client.LeaderClient;
import com.dp.notary.blockchain.api.client.ReplicaClient;
import com.dp.notary.blockchain.auth.AuthService;
import com.dp.notary.blockchain.auth.SessionService;
import com.dp.notary.blockchain.behavior.RoleBehavior;
import com.dp.notary.blockchain.blockchain.BlockchainService;
import com.dp.notary.blockchain.blockchain.model.*;
import com.dp.notary.blockchain.config.NotaryProperties;
import jakarta.annotation.PostConstruct;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static org.apache.logging.log4j.util.Strings.trimToNull;

@Component
public class TransactionsListController {

    // ===== FILTERS (one row) =====
    @FXML private TextField filterCreatedBy;
    @FXML private TextField filterInitiator;
    @FXML private TextField filterTarget;

    @FXML private ComboBox<TypeFilterItem> filterType;

    // ===== TABLE =====
    @FXML private TableView<TransactionRowVM> table;

    @FXML private TableColumn<TransactionRowVM, String> colTime;
//    @FXML private TableColumn<TransactionRowVM, Number> colId;
    @FXML private TableColumn<TransactionRowVM, String> colType;
    @FXML private TableColumn<TransactionRowVM, String> colCreatedBy;
    @FXML private TableColumn<TransactionRowVM, String> colInitiator;
    @FXML private TableColumn<TransactionRowVM, String> colTarget;
    @FXML private TableColumn<TransactionRowVM, Number> colAmount;

    @FXML
    private HBox paginationTop;

    @FXML
    private HBox paginationBottom;

    // ===== DETAILS =====
    @FXML private Label detailsHint;

//    @FXML private Label detailsId;
    @FXML private Label detailsTime;

    @FXML private Label detailsType;
    @FXML private Label detailsCreatedBy;
    @FXML private Label detailsInitiator;
    @FXML private Label detailsTarget;
    @FXML private Label detailsAmount;

    // ===== ACTIONS =====
    @FXML private Button editBtn;
    @FXML private Button deleteBtn;

    @FXML private Button approveBtn;
    @FXML private Button declineBtn;

//    @FXML private VBox declineBox;
//    @FXML private TextArea declineCommentArea;

    @FXML private Button resubmitBtn;

    @Value("${ui.pageSize}")
    private int PAGE_SIZE;

    // ===== DATA PIPELINE =====
    private int currentPage = 0;
    // observableList - notifies if there are changes
    private final ObservableList<TransactionRowVM> pageTransactions = FXCollections.observableArrayList();


    // ===== MODE / CALLBACKS =====
    private Mode mode = Mode.APPROVED;

    // ===== FILTERS ======
    private String createdByFilter;
    private String initiatorFilter;
    private String targetFilter;
    private TransactionType typeFilter;


    private Actions actions = new Actions() {
        @Override public void onEdit(TransactionRowVM tx) { System.out.println("Edit: ");}
        @Override public void onDelete() { System.out.println("Delete: "); }
        @Override public void onApprove() { System.out.println("Approve: "); }
        @Override public void onDecline() { System.out.println("Decline: "); }
        @Override public void onResubmit() { System.out.println("Resubmit: "); }
    };

    DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")
                    .withZone(ZoneId.systemDefault());

    // ================== MODULES ==================
    private AuthService authService;
    private BlockchainService blockchainService;
    private final SessionService sessionService;
    private final RoleBehavior roleBehavior;

    // ================== PUBLIC API ==================

    TransactionsListController(AuthService authService, BlockchainService blockchainService, NotaryProperties props, SessionService sessionService, LeaderClient leaderClient, ReplicaClient replicaClient, RoleBehavior roleBehavior){
        this.authService = authService;
        this.blockchainService = blockchainService;
        this.sessionService = sessionService;
        this.roleBehavior = roleBehavior;
    }

    public void setMode(Mode mode) {
        this.mode = Objects.requireNonNull(mode);
        createdByFilter = null;
        initiatorFilter = null;
        targetFilter = null;
        typeFilter = null;
        loadPage(0);
        updateDetails(null);
        refreshActions();        // buttons depend on mode + status
        updateDetails(table.getSelectionModel().getSelectedItem());
    }

    public void setActions(Actions actions) {
        this.actions = (actions != null) ? actions : this.actions;
    }

    // ================== INIT ==================

    @FXML
    private void initialize() {
        setupColumns();
        setupCombos();
        setupDataPipeline();
        setupListeners();

        resetDetails();
        refreshActions();

        loadPage(0);
    }

    private void setupColumns() {
        colTime.setCellValueFactory(c -> new SimpleStringProperty(TIME_FMT.format(c.getValue().timestamp())));
//        colId.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().id()));
        colType.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().type().name()));
        colCreatedBy.setCellValueFactory(c -> new SimpleStringProperty(nullToDash(c.getValue().createdBy())));
        colInitiator.setCellValueFactory(c -> new SimpleStringProperty(nullToDash(c.getValue().initiator())));
        colTarget.setCellValueFactory(c -> new SimpleStringProperty(nullToDash(c.getValue().target())));
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));

        // TableView built-in sorting works via comparatorProperty binding (we do it below)
        // Ensure columns are sortable (default true), keep it explicit:
        colTime.setSortable(true);
//        colId.setSortable(true);
        colType.setSortable(true);
        colCreatedBy.setSortable(true);
        colInitiator.setSortable(true);
        colTarget.setSortable(true);
        colAmount.setSortable(true);
    }

    private void setupCombos() {
        // Type filter: ALL + enum values
        filterType.setItems(FXCollections.observableArrayList(
                TypeFilterItem.all(),
                TypeFilterItem.of(TransactionType.PURCHASE),
                TypeFilterItem.of(TransactionType.SELL),
                TypeFilterItem.of(TransactionType.GRANT),
                TypeFilterItem.of(TransactionType.DIVIDEND)
        ));
        filterType.getSelectionModel().selectFirst();
    }

    private void setupDataPipeline() {
        SortedList<TransactionRowVM> sorted = new SortedList<>(pageTransactions);

        // KEY PART: built-in table sorting
        sorted.comparatorProperty().bind(table.comparatorProperty());

        table.setItems(sorted);

    }

    private void setupListeners() {
        // Selection -> details + buttons
        table.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            updateDetails(n);
            refreshActions();
//            hideDeclineBox();
        });
    }

    // ================== FILTERING ==================

    @FXML
    private void onClearFilters() {
        if (!sessionService.ensureAuthenticated())
            return;

        filterCreatedBy.clear();
        filterInitiator.clear();
        filterTarget.clear();

        filterType.getSelectionModel().selectFirst();

        onApplyFilters();
    }

    @FXML
    private void onApplyFilters() {
        if (!sessionService.ensureAuthenticated())
            return;

        // apply filters
        createdByFilter = trimToNull(filterCreatedBy.getText());
        initiatorFilter = trimToNull(filterInitiator.getText());
        targetFilter    = trimToNull(filterTarget.getText());
        typeFilter = filterType.getValue() != null ? filterType.getValue().type() : null;

        loadPage(0);
    }

    // ================== DETAILS ==================

    private void resetDetails() {
        detailsHint.setText("Select a transaction from the table.");
        detailsTime.setText("—");
        detailsType.setText("—");
        detailsCreatedBy.setText("—");
        detailsInitiator.setText("—");
        detailsTarget.setText("—");
        detailsAmount.setText("—");
    }

    private void updateDetails(TransactionRowVM tx) {
        if (tx == null) {
            resetDetails();
            return;
        }

        detailsHint.setText("Selected transaction:");
        detailsTime.setText(TIME_FMT.format(tx.timestamp()));
        detailsType.setText(tx.type().name());
        detailsCreatedBy.setText(nullToDash(tx.createdBy()));
        detailsInitiator.setText(nullToDash(tx.initiator()));
        detailsTarget.setText(nullToDash(tx.target()));
        detailsAmount.setText(tx.amount());
    }

    // ================== MODE UI ==================

    private void refreshActions() {
        TransactionRowVM selected = table.getSelectionModel().getSelectedItem();

        // hide all by default
        hide(editBtn);
        hide(deleteBtn);
        hide(approveBtn);
        hide(declineBtn);
        hide(resubmitBtn);
//        hideDeclineBox();

        if (selected == null) return;

        switch (mode) {
            case APPROVED -> {
                // no actions
            }
            case DRAFTS -> {
                show(editBtn);
                show(deleteBtn);
            }
            case PENDING -> {
                show(approveBtn);
                show(declineBtn);
            }
            case MY_SUBMITTED -> {
                // no actions
            }
            case DECLINED -> {
                show(editBtn);
                show(deleteBtn);
                show(resubmitBtn);
            }
        }
    }

    // ================== ACTION HANDLERS ==================

    @FXML
    private void onEdit() {
        if (!sessionService.ensureAuthenticated())
            return;

        TransactionRowVM tx = table.getSelectionModel().getSelectedItem();

        if (tx == null) return;
        actions.onEdit(tx);
    }

    @FXML
    private void onDelete() {
        if (!sessionService.ensureAuthenticated())
            return;
        TransactionRowVM tx = table.getSelectionModel().getSelectedItem();

        if (tx == null) return;
        if (!(tx.status == TransactionStatus.DRAFT || tx.status == TransactionStatus.DECLINED)) return;
        roleBehavior.deleteTransaction(tx.id());
        actions.onDelete();
    }

    @FXML
    private void onApprove() {
        if (!sessionService.ensureAuthenticated())
            return;
        TransactionRowVM tx = table.getSelectionModel().getSelectedItem();
        if (tx == null) return;
        roleBehavior.approveTransaction(tx.id());
        actions.onApprove();
    }

    @FXML
    private void onDecline() {
        if (!sessionService.ensureAuthenticated())
            return;

//        if (table.getSelectionModel().getSelectedItem() == null) return;
//        declineBox.setVisible(true);
//        declineBox.setManaged(true);
//        declineCommentArea.requestFocus();
        TransactionRowVM tx = table.getSelectionModel().getSelectedItem();
        if (tx == null) return;
        roleBehavior.declineTransaction(tx.id());
        actions.onDecline();
    }

//    @FXML
//    private void onConfirmDecline() {
//        TransactionRowVM tx = table.getSelectionModel().getSelectedItem();
//        if (tx == null) return;
//
//        String comment = declineCommentArea.getText() == null ? "" : declineCommentArea.getText().trim();
//        actions.onDecline(tx, comment);
//
//        declineCommentArea.clear();
//        hideDeclineBox();
//    }

    @FXML
    private void onResubmit() {
        if (!sessionService.ensureAuthenticated())
            return;
        TransactionRowVM tx = table.getSelectionModel().getSelectedItem();
        if (tx == null) return;
        roleBehavior.resubmit(tx.id());
        actions.onResubmit();
    }

//    private void hideDeclineBox() {
//        declineBox.setVisible(false);
//        declineBox.setManaged(false);
//        declineCommentArea.clear();
//    }

    // ================== UTILS ==================

    private static boolean containsIgnoreCase(String haystack, String needle) {
        if (needle == null || needle.isBlank()) return true;
        if (haystack == null) return false;
        return haystack.toLowerCase(Locale.ROOT).contains(needle.trim().toLowerCase(Locale.ROOT));
    }

    private static String nullToDash(String s) {
        return (s == null || s.isBlank()) ? "—" : s;
    }

    private static void show(Control c) {
        c.setVisible(true);
        c.setManaged(true);
        c.setDisable(false);
    }

    private static void hide(Control c) {
        c.setVisible(false);
        c.setManaged(false);
        c.setDisable(true);
    }

    // ================== PAGINATION ==================

    private void loadPage(int page) {
        if (!sessionService.ensureAuthenticated())
            return;
//        int fromBlock = page * BLOCKS_PER_PAGE;
//        int blockCount = BLOCKS_PER_PAGE;

        // TODO: подключить функцию - вроде уже подключено, но если поменяется то поменять
        // mode APPROVED = transaction status APPROVED
        // mode DRAFTS = transaction status DRAFT for current user - i guess you dont have it in this file do you need to add ??
        // mode PENDING = transaction status SUBMITTED any user
        // mode MY_SUBMITTED = transaction status SUBMITTED (same as above) DECLINED for current user - i guess you dont have it in this file do you need to add ??

        pageTransactions.clear();

        List<TransactionEntity> txs = new ArrayList<>();
        if (mode == Mode.APPROVED) {
            txs = blockchainService.getApprovedTransactions(page, PAGE_SIZE, createdByFilter, initiatorFilter, targetFilter, typeFilter);
        }
        else if (mode == Mode.PENDING)
            txs = blockchainService.getStatusTransactions(page, PAGE_SIZE, resolveStatus(), createdByFilter, initiatorFilter, targetFilter, typeFilter);
        else
            txs = blockchainService.getStatusTransactions(page, PAGE_SIZE, resolveStatus(), authService.getNameFromToken(App.get().getToken()), initiatorFilter, targetFilter, typeFilter);


        pageTransactions.setAll(txs.stream().map(TransactionRowVM::new).toList());

        currentPage = page;
        updatePagination();
    }

    private int getPageCount() {
        String username = authService.getNameFromToken(App.get().getToken());
        int total;
        switch (mode) {
            case APPROVED -> total = blockchainService.totalApproved(createdByFilter, initiatorFilter, targetFilter, typeFilter);
            case DRAFTS -> total = blockchainService.totalDraft(username, initiatorFilter, targetFilter, typeFilter);
            case PENDING -> total = blockchainService.totalSubmitted(createdByFilter, initiatorFilter, targetFilter, typeFilter);
            case MY_SUBMITTED -> total = blockchainService.totalSubmitted(username, initiatorFilter, targetFilter, typeFilter);
            case DECLINED -> total = blockchainService.totalDeclined(username, initiatorFilter, targetFilter, typeFilter);
            default -> total = 0;
        }

        return (int) Math.ceil((double) total / PAGE_SIZE);
    }

    private boolean shouldShowPage(int page, int pageCount) {
        return page == 0
                || page == pageCount - 1
                || Math.abs(page - currentPage) <= 1;
    }

    private void renderPagination(HBox box) {
        box.getChildren().clear();

        int pageCount = getPageCount();
        if (pageCount <= 1) return;

        // <
        Button prev = new Button("<");
        prev.setDisable(currentPage == 0);
        prev.setOnAction(e -> {
            loadPage(currentPage - 1);
        });
        box.getChildren().add(prev);

        for (int i = 0; i < pageCount; i++) {
            if (shouldShowPage(i, pageCount)) {
                int pageIndex = i;

                Button btn = new Button(String.valueOf(i + 1));
                btn.setDisable(i == currentPage);
                btn.setOnAction(e -> {
                    loadPage(pageIndex);
                });

                box.getChildren().add(btn);
            } else if (i == currentPage + 2) {
                box.getChildren().add(new Label("..."));
            }
        }

        // >
        Button next = new Button(">");
        next.setDisable(currentPage >= pageCount - 1);
        next.setOnAction(e -> {
            loadPage(currentPage + 1);
        });
        box.getChildren().add(next);
    }

    private void updatePagination() {
        renderPagination(paginationTop);
        renderPagination(paginationBottom);
    }

    // ================== TYPES ==================

    public enum
    Mode {
        APPROVED,
        DRAFTS,
        PENDING,
        MY_SUBMITTED,
        DECLINED
    }

    private TransactionStatus resolveStatus() {
        return switch (mode) {
            case APPROVED -> TransactionStatus.APPROVED;
            case PENDING, MY_SUBMITTED -> TransactionStatus.SUBMITTED;
            case DRAFTS -> TransactionStatus.DRAFT;
            case DECLINED -> TransactionStatus.DECLINED;
        };
    }
    
    public record TypeFilterItem(TransactionType type, String label) {
        public static TypeFilterItem all() { return new TypeFilterItem(null, "All"); }
        public static TypeFilterItem of(TransactionType t) { return new TypeFilterItem(t, t.name()); }
        @Override public String toString() { return label; }
    }

    public interface Actions {
        void onEdit(TransactionRowVM tx);
        void onDelete();
        void onApprove();
        void onDecline();
        void onResubmit();
    }

    public static final class  TransactionRowVM {
        private final SimpleStringProperty id = new SimpleStringProperty();
        private final SimpleLongProperty timestampEpoch = new SimpleLongProperty();

        private final TransactionStatus status;
        private final TransactionType type;

        private final SimpleStringProperty createdBy = new SimpleStringProperty();
        private final SimpleStringProperty initiator = new SimpleStringProperty();
        private final SimpleStringProperty target = new SimpleStringProperty();

        private final SimpleStringProperty amount = new SimpleStringProperty();

        public TransactionRowVM(String id,
                             Instant timestamp,
                             TransactionStatus status,
                             TransactionType type,
                             String createdBy,
                             String initiator,
                             String target,
                             BigDecimal amount) {
            this.id.set(id);
            this.timestampEpoch.set(timestamp.getEpochSecond());
            this.status = status;
            this.type = type;
            this.createdBy.set(createdBy);
            this.initiator.set(initiator);
            this.target.set(target);
            this.amount.set(amount.toString());
        }
        public TransactionRowVM(TransactionEntity tx) {
            this.id.set(tx.getTxId());
            this.timestampEpoch.set(tx.getTimestamp().getEpochSecond());
            this.status = tx.getStatus();
            this.type = tx.getType();
            this.createdBy.set(tx.getCreatedBy());
            this.target.set(tx.getTarget());
            this.amount.set(tx.getAmount().toString());
            this.initiator.set(tx.getInitiator());
        }

        public String id() { return id.get(); }
        public Instant timestamp() { return Instant.ofEpochSecond(timestampEpoch.get()); }
        public TransactionStatus status() { return status; }
        public TransactionType type() { return type; }
        public String createdBy() { return createdBy.get(); }
        public String initiator() { return initiator.get(); }
        public String target() { return target.get(); }
        public String amount() { return amount.get(); }

    }
}
