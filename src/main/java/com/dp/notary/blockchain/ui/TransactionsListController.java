package com.dp.notary.blockchain.ui;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Locale;
import java.util.Objects;

public class TransactionsListController {

    // ===== FILTERS (one row) =====
    @FXML private TextField filterCreatedBy;
    @FXML private TextField filterInitiator;
    @FXML private TextField filterTarget;

    @FXML private ComboBox<TypeFilterItem> filterType;
    @FXML private ComboBox<StatusFilterItem> filterStatus; // visible only in MY_SUBMITTED

    // ===== TABLE =====
    @FXML private TableView<TransactionVM> table;

    @FXML private TableColumn<TransactionVM, String> colTime;
    @FXML private TableColumn<TransactionVM, String> colId;
    @FXML private TableColumn<TransactionVM, String> colStatus; // only MY_SUBMITTED
    @FXML private TableColumn<TransactionVM, String> colType;
    @FXML private TableColumn<TransactionVM, String> colCreatedBy;
    @FXML private TableColumn<TransactionVM, String> colInitiator;
    @FXML private TableColumn<TransactionVM, String> colTarget;
    @FXML private TableColumn<TransactionVM, Number> colAmount;

    // ===== DETAILS =====
    @FXML private Label detailsHint;

    @FXML private Label detailsId;
    @FXML private Label detailsTime;

    @FXML private Label detailsStatusLabel;
    @FXML private Label detailsStatus;

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

    @FXML private VBox declineBox;
    @FXML private TextArea declineCommentArea;

    @FXML private Button resubmitBtn;

    // ===== DATA PIPELINE =====
    private final ObservableList<TransactionVM> master = FXCollections.observableArrayList();
    private FilteredList<TransactionVM> filtered;
    private SortedList<TransactionVM> sorted;

    // ===== MODE / CALLBACKS =====
    private Mode mode = Mode.APPROVED;

    private Actions actions = new Actions() {
        @Override public void onEdit(TransactionVM tx) { System.out.println("Edit: " + tx.id()); }
        @Override public void onDelete(TransactionVM tx) { System.out.println("Delete: " + tx.id()); }
        @Override public void onApprove(TransactionVM tx) { System.out.println("Approve: " + tx.id()); }
        @Override public void onDecline(TransactionVM tx, String comment) { System.out.println("Decline: " + tx.id() + " comment=" + comment); }
        @Override public void onResubmit(TransactionVM tx) { System.out.println("Resubmit: " + tx.id()); }
    };

    private static final DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                    .withLocale(Locale.US)
                    .withZone(ZoneId.systemDefault());

    // ================== PUBLIC API ==================

    public void setItems(ObservableList<TransactionVM> items) {
        master.setAll(items);
    }

    public void setMode(Mode mode) {
        this.mode = Objects.requireNonNull(mode);
        applyModeUI();
        reapplyFilter();         // status filter affects predicate only in MY_SUBMITTED
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

        applyModeUI();
        resetDetails();
        refreshActions();

        // demo data (можешь убрать, когда подключишь API)
        if (master.isEmpty()) {
            master.addAll(
                    TransactionVM.demo("T-001", Instant.now().minusSeconds(900), TxStatus.SUBMITTED, TransactionType.PURCHASE,
                            "Alice Leader", "John", "Kate", 120.0),
                    TransactionVM.demo("T-002", Instant.now().minusSeconds(700), TxStatus.DECLINED, TransactionType.GRANT,
                            "Bob Replica", "Kate", "John", 50.0),
                    TransactionVM.demo("T-003", Instant.now().minusSeconds(500), TxStatus.SUBMITTED, TransactionType.DIVIDEND,
                            "Alice Leader", "Company", "Owners", 999.0)
            );
        }
    }

    private void setupColumns() {
        colTime.setCellValueFactory(c -> new SimpleStringProperty(TIME_FMT.format(c.getValue().timestamp())));
        colId.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().id()));
        colStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().status().name()));
        colType.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().type().name()));
        colCreatedBy.setCellValueFactory(c -> new SimpleStringProperty(nullToDash(c.getValue().createdBy())));
        colInitiator.setCellValueFactory(c -> new SimpleStringProperty(nullToDash(c.getValue().initiator())));
        colTarget.setCellValueFactory(c -> new SimpleStringProperty(nullToDash(c.getValue().target())));
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));

        // TableView built-in sorting works via comparatorProperty binding (we do it below)
        // Ensure columns are sortable (default true), keep it explicit:
        colTime.setSortable(true);
        colId.setSortable(true);
        colType.setSortable(true);
        colCreatedBy.setSortable(true);
        colInitiator.setSortable(true);
        colTarget.setSortable(true);
        colAmount.setSortable(true);
        colStatus.setSortable(false); // ты хотела статус НЕ сортировать
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

        // Status filter: ALL + SUBMITTED/DECLINED
        filterStatus.setItems(FXCollections.observableArrayList(
                StatusFilterItem.all(),
                StatusFilterItem.of(TxStatus.SUBMITTED),
                StatusFilterItem.of(TxStatus.DECLINED)
        ));
        filterStatus.getSelectionModel().selectFirst();
    }

    private void setupDataPipeline() {
        filtered = new FilteredList<>(master, tx -> true);
        sorted = new SortedList<>(filtered);

        // KEY PART: built-in table sorting
        sorted.comparatorProperty().bind(table.comparatorProperty());

        table.setItems(sorted);

        // hide status column by default (enabled in MY_SUBMITTED)
        colStatus.setVisible(false);
    }

    private void setupListeners() {
        // Filters
        filterCreatedBy.textProperty().addListener((obs, o, n) -> reapplyFilter());
        filterInitiator.textProperty().addListener((obs, o, n) -> reapplyFilter());
        filterTarget.textProperty().addListener((obs, o, n) -> reapplyFilter());
        filterType.valueProperty().addListener((obs, o, n) -> reapplyFilter());
        filterStatus.valueProperty().addListener((obs, o, n) -> reapplyFilter());

        // Selection -> details + buttons
        table.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            updateDetails(n);
            refreshActions();
            hideDeclineBox();
        });
    }

    // ================== FILTERING ==================

    private void reapplyFilter() {
        filtered.setPredicate(this::passesFilters);
        refreshActions();
        // если выбранный элемент отфильтровался — сбросим детали
        TransactionVM sel = table.getSelectionModel().getSelectedItem();
        if (sel == null || !filtered.contains(sel)) {
            resetDetails();
        }
    }

    private boolean passesFilters(TransactionVM tx) {
        // Type (enum) filter
        TypeFilterItem tf = filterType.getValue();
        if (tf != null && tf.type() != null) {
            if (tx.type() != tf.type()) return false;
        }

        // Status (enum) filter only in MY_SUBMITTED
        if (mode == Mode.MY_SUBMITTED) {
            StatusFilterItem sf = filterStatus.getValue();
            if (sf != null && sf.status() != null) {
                if (tx.status() != sf.status()) return false;
            }
        }

        // Partial text filters (case-insensitive)
        if (!containsIgnoreCase(tx.createdBy(), filterCreatedBy.getText())) return false;
        if (!containsIgnoreCase(tx.initiator(), filterInitiator.getText())) return false;
        if (!containsIgnoreCase(tx.target(), filterTarget.getText())) return false;

        return true;
    }

    @FXML
    private void onClearFilters() {
        filterCreatedBy.clear();
        filterInitiator.clear();
        filterTarget.clear();

        filterType.getSelectionModel().selectFirst();
        filterStatus.getSelectionModel().selectFirst();

        reapplyFilter();
    }

    // ================== DETAILS ==================

    private void resetDetails() {
        detailsHint.setText("Select a transaction from the table.");
        detailsId.setText("—");
        detailsTime.setText("—");
        detailsStatus.setText("—");
        detailsType.setText("—");
        detailsCreatedBy.setText("—");
        detailsInitiator.setText("—");
        detailsTarget.setText("—");
        detailsAmount.setText("—");
    }

    private void updateDetails(TransactionVM tx) {
        if (tx == null) {
            resetDetails();
            return;
        }

        detailsHint.setText("Selected transaction:");
        detailsId.setText(tx.id());
        detailsTime.setText(TIME_FMT.format(tx.timestamp()));
        detailsType.setText(tx.type().name());
        detailsCreatedBy.setText(nullToDash(tx.createdBy()));
        detailsInitiator.setText(nullToDash(tx.initiator()));
        detailsTarget.setText(nullToDash(tx.target()));
        detailsAmount.setText(String.valueOf(tx.amount()));

        if (mode == Mode.MY_SUBMITTED) {
            detailsStatus.setText(tx.status().name());
        } else {
            detailsStatus.setText("—");
        }
    }

    // ================== MODE UI ==================

    private void applyModeUI() {
        boolean showStatusUI = (mode == Mode.MY_SUBMITTED);

        // Status filter combobox shown only in MY_SUBMITTED
        filterStatus.setVisible(showStatusUI);
        filterStatus.setManaged(showStatusUI);

        // Status column visible only in MY_SUBMITTED
        colStatus.setVisible(showStatusUI);

        // Status in details visible only in MY_SUBMITTED
        detailsStatusLabel.setVisible(showStatusUI);
        detailsStatusLabel.setManaged(showStatusUI);
        detailsStatus.setVisible(showStatusUI);
        detailsStatus.setManaged(showStatusUI);

        // Also hide decline box when switching mode
        hideDeclineBox();
    }

    private void refreshActions() {
        TransactionVM selected = table.getSelectionModel().getSelectedItem();

        // hide all by default
        hide(editBtn);
        hide(deleteBtn);
        hide(approveBtn);
        hide(declineBtn);
        hide(resubmitBtn);
        hideDeclineBox();

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
                if (selected.status() == TxStatus.DECLINED) {
                    show(editBtn);
                    show(deleteBtn);
                    show(resubmitBtn);
                }
            }
        }
    }

    // ================== ACTION HANDLERS ==================

    @FXML
    private void onEdit() {
        TransactionVM tx = table.getSelectionModel().getSelectedItem();
        if (tx == null) return;
        actions.onEdit(tx);
    }

    @FXML
    private void onDelete() {
        TransactionVM tx = table.getSelectionModel().getSelectedItem();
        if (tx == null) return;
        actions.onDelete(tx);
    }

    @FXML
    private void onApprove() {
        TransactionVM tx = table.getSelectionModel().getSelectedItem();
        if (tx == null) return;
        actions.onApprove(tx);
    }

    @FXML
    private void onDecline() {
        if (table.getSelectionModel().getSelectedItem() == null) return;
        declineBox.setVisible(true);
        declineBox.setManaged(true);
        declineCommentArea.requestFocus();
    }

    @FXML
    private void onConfirmDecline() {
        TransactionVM tx = table.getSelectionModel().getSelectedItem();
        if (tx == null) return;

        String comment = declineCommentArea.getText() == null ? "" : declineCommentArea.getText().trim();
        actions.onDecline(tx, comment);

        declineCommentArea.clear();
        hideDeclineBox();
    }

    @FXML
    private void onResubmit() {
        TransactionVM tx = table.getSelectionModel().getSelectedItem();
        if (tx == null) return;
        actions.onResubmit(tx);
    }

    private void hideDeclineBox() {
        declineBox.setVisible(false);
        declineBox.setManaged(false);
        declineCommentArea.clear();
    }

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

    // ================== TYPES ==================

    public enum Mode {
        APPROVED,
        DRAFTS,
        PENDING,
        MY_SUBMITTED
    }

    public enum TxStatus {
        SUBMITTED, DECLINED
    }

    public enum TransactionType {
        PURCHASE, SELL, GRANT, DIVIDEND
    }

    public record TypeFilterItem(TransactionType type, String label) {
        public static TypeFilterItem all() { return new TypeFilterItem(null, "All"); }
        public static TypeFilterItem of(TransactionType t) { return new TypeFilterItem(t, t.name()); }
        @Override public String toString() { return label; }
    }

    public record StatusFilterItem(TxStatus status, String label) {
        public static StatusFilterItem all() { return new StatusFilterItem(null, "All"); }
        public static StatusFilterItem of(TxStatus s) { return new StatusFilterItem(s, s.name()); }
        @Override public String toString() { return label; }
    }

    public interface Actions {
        void onEdit(TransactionVM tx);
        void onDelete(TransactionVM tx);
        void onApprove(TransactionVM tx);
        void onDecline(TransactionVM tx, String comment);
        void onResubmit(TransactionVM tx);
    }

    public static final class TransactionVM {
        private final SimpleStringProperty id = new SimpleStringProperty();
        private final SimpleLongProperty timestampEpoch = new SimpleLongProperty();

        private final TxStatus status;
        private final TransactionType type;

        private final SimpleStringProperty createdBy = new SimpleStringProperty();
        private final SimpleStringProperty initiator = new SimpleStringProperty();
        private final SimpleStringProperty target = new SimpleStringProperty();

        private final SimpleDoubleProperty amount = new SimpleDoubleProperty();

        public TransactionVM(String id,
                             Instant timestamp,
                             TxStatus status,
                             TransactionType type,
                             String createdBy,
                             String initiator,
                             String target,
                             double amount) {
            this.id.set(id);
            this.timestampEpoch.set(timestamp.getEpochSecond());
            this.status = status;
            this.type = type;
            this.createdBy.set(createdBy);
            this.initiator.set(initiator);
            this.target.set(target);
            this.amount.set(amount);
        }

        public String id() { return id.get(); }
        public Instant timestamp() { return Instant.ofEpochSecond(timestampEpoch.get()); }
        public TxStatus status() { return status; }
        public TransactionType type() { return type; }
        public String createdBy() { return createdBy.get(); }
        public String initiator() { return initiator.get(); }
        public String target() { return target.get(); }
        public double amount() { return amount.get(); }

        // for PropertyValueFactory("amount")
        public double getAmount() { return amount.get(); }

        public static TransactionVM demo(String id, Instant ts, TxStatus st, TransactionType tp,
                                         String createdBy, String initiator, String target, double amount) {
            return new TransactionVM(id, ts, st, tp, createdBy, initiator, target, amount);
        }
    }
}
