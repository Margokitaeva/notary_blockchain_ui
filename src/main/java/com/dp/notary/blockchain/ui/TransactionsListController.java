package com.dp.notary.blockchain.ui;

import com.dp.notary.blockchain.auth.AuthService;
import com.dp.notary.blockchain.blockchain.model.*;
import jakarta.annotation.PostConstruct;
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
import javafx.scene.layout.HBox;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
@Component
public class TransactionsListController {

    // ===== FILTERS (one row) =====
    @FXML private TextField filterCreatedBy;
    @FXML private TextField filterInitiator;
    @FXML private TextField filterTarget;

    @FXML private ComboBox<TypeFilterItem> filterType;
    @FXML private ComboBox<StatusFilterItem> filterStatus; // visible only in MY_SUBMITTED

    // ===== TABLE =====
    @FXML private TableView<TransactionRowVM> table;

    @FXML private TableColumn<TransactionRowVM, String> colTime;
//    @FXML private TableColumn<TransactionRowVM, String> colId;
    @FXML private TableColumn<TransactionRowVM, String> colStatus; // only MY_SUBMITTED
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

//    @FXML private VBox declineBox;
//    @FXML private TextArea declineCommentArea;

    @FXML private Button resubmitBtn;

    @Value("${ui.pageSize}")
    private int PAGE_SIZE;
    @Value("${ui.txPerBlock}")
    private int TX_PER_BLOCK;

    private int BLOCKS_PER_PAGE;

    private int currentPage = 0;
    private final ObservableList<TransactionRowVM> pageTransactions = FXCollections.observableArrayList();

    // ===== DATA PIPELINE =====
    // observableList - notifies if there are changes
    private final ObservableList<TransactionRowVM> master = FXCollections.observableArrayList();
    private FilteredList<TransactionRowVM> filtered;
    private SortedList<TransactionRowVM> sorted;

    // ===== MODE / CALLBACKS =====
    private Mode mode = Mode.APPROVED;

    private Actions actions = new Actions() {
        @Override public void onEdit(TransactionRowVM tx) { System.out.println("Edit: " + tx.id()); }
        @Override public void onDelete(TransactionRowVM tx) { System.out.println("Delete: " + tx.id()); }
        @Override public void onApprove(TransactionRowVM tx) { System.out.println("Approve: " + tx.id()); }
        @Override public void onDecline(TransactionRowVM tx) { System.out.println("Decline: " + tx.id()); }
        @Override public void onResubmit(TransactionRowVM tx) { System.out.println("Resubmit: " + tx.id()); }
    };

    private static final DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                    .withLocale(Locale.US)
                    .withZone(ZoneId.systemDefault());

    // ================== MODULES ==================
    private AuthService authService;
    // ================== PUBLIC API ==================

    TransactionsListController(AuthService authService){
        this.authService = authService;
    }
    @PostConstruct
    private void init(){
        BLOCKS_PER_PAGE = PAGE_SIZE / TX_PER_BLOCK;
    }
    public void setItems(ObservableList<TransactionRowVM> items) {
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

//        loadTransactionsFromBlocks();
        loadPage(0);
    }

    private void setupColumns() {
        colTime.setCellValueFactory(c -> new SimpleStringProperty(TIME_FMT.format(c.getValue().timestamp())));
//        colId.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().id()));
        colStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().status().name()));
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
        colStatus.setSortable(false); // ты хотела статус НЕ сортировать
    }

    private void setupCombos() {
        // Type filter: ALL + enum values
        filterType.setItems(FXCollections.observableArrayList(
                TypeFilterItem.all(),
                TypeFilterItem.of(TransactionType.PURCHASE),
                TypeFilterItem.of(TransactionType.SELL),
                TypeFilterItem.of(TransactionType.GRANT),
                TypeFilterItem.of(TransactionType.DIVIDENT)
        ));
        filterType.getSelectionModel().selectFirst();

        // Status filter: ALL + SUBMITTED/DECLINED
        filterStatus.setItems(FXCollections.observableArrayList(
                StatusFilterItem.all(),
                StatusFilterItem.of(TransactionStatus.SUBMITTED),
                StatusFilterItem.of(TransactionStatus.DECLINED)
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
//            hideDeclineBox();
        });
    }

    // ================== FILTERING ==================

    private void reapplyFilter() {
        filtered.setPredicate(this::passesFilters);
        refreshActions();
        // если выбранный элемент отфильтровался — сбросим детали
        TransactionRowVM sel = table.getSelectionModel().getSelectedItem();
        if (sel == null || !filtered.contains(sel)) {
            resetDetails();
        }
    }

    private boolean passesFilters(TransactionRowVM tx) {
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

    private void updateDetails(TransactionRowVM tx) {
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
//        hideDeclineBox();
    }

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
                if (selected.status() == TransactionStatus.DECLINED) {
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
        TransactionRowVM tx = table.getSelectionModel().getSelectedItem();
        if (tx == null) return;
        actions.onEdit(tx);
    }

    @FXML
    private void onDelete() {
        TransactionRowVM tx = table.getSelectionModel().getSelectedItem();
        if (tx == null) return;
        actions.onDelete(tx);
    }

    @FXML
    private void onApprove() {
        TransactionRowVM tx = table.getSelectionModel().getSelectedItem();
        if (tx == null) return;
        actions.onApprove(tx);
    }

    @FXML
    private void onDecline() {
//        if (table.getSelectionModel().getSelectedItem() == null) return;
//        declineBox.setVisible(true);
//        declineBox.setManaged(true);
//        declineCommentArea.requestFocus();
        TransactionRowVM tx = table.getSelectionModel().getSelectedItem();
        if (tx == null) return;

        actions.onDecline(tx);
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
        TransactionRowVM tx = table.getSelectionModel().getSelectedItem();
        if (tx == null) return;
        actions.onResubmit(tx);
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
        int fromBlock = page * BLOCKS_PER_PAGE;
        int blockCount = BLOCKS_PER_PAGE;

        // TODO: get blockCount number of blocks (instead of fetchBlocksFromServer), use this.Mode
        // mode APPROVED = transaction status APPROVED
        // mode DRAFTS = transaction status DRAFT for current user - i guess you dont have it in this file do you need to add ??
        // mode PENDING = transaction status SUBMITTED any user
        // mode MY_SUBMITTED = transaction status SUBMITTED (same as above) DECLINED for current user - i guess you dont have it in this file do you need to add ??

        List<BlockEntity> blocks = blockchain.getBlocks(fromBlock, blockCount);

        pageTransactions.clear();

        for (BlockEntity block : blocks) {
            for (TransactionEntity tx : block.getTransactions()) {
                pageTransactions.add(new TransactionRowVM(tx));
            }
        }

        currentPage = page;
        updateTable();
        updatePagination();
    }

    private int getPageCount() {
        // number of pages = number of blocks * 5 / PAGE_SIZE
        // если есть у Кати переменная,в  которой написано сколько транзакций в блоке, то можно вместо 5 вписать эту переменную
        // либо если мы планируем сделать много транзакций в блоке потом то тогда надо делать пересчет чтобы одномоментно было 15-20 транзакций отображено

        long totalBlocks = blockchain.getChainSize();
        return (int) Math.ceil((double) totalBlocks / BLOCKS_PER_PAGE);
    }

    private void updateTable() {
        table.setItems(pageTransactions);
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
//            currentPage--;
//            updateAll();
        });
        box.getChildren().add(prev);

        for (int i = 0; i < pageCount; i++) {
            if (shouldShowPage(i, pageCount)) {
                int pageIndex = i;

                Button btn = new Button(String.valueOf(i + 1));
                btn.setDisable(i == currentPage);
                btn.setOnAction(e -> {
                    loadPage(pageIndex);
//                    currentPage = pageIndex;
//                    updateAll();
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
//            currentPage++;
//            updateAll();
        });
        box.getChildren().add(next);
    }

    private void updateAll() {
        updateTable();
        updatePagination();
    }

    private void updatePagination() {
        renderPagination(paginationTop);
        renderPagination(paginationBottom);
    }

    // ================== TYPES ==================

    public enum Mode {
        APPROVED,
        DRAFTS,
        PENDING,
        MY_SUBMITTED
    }

    public record TypeFilterItem(TransactionType type, String label) {
        public static TypeFilterItem all() { return new TypeFilterItem(null, "All"); }
        public static TypeFilterItem of(TransactionType t) { return new TypeFilterItem(t, t.name()); }
        @Override public String toString() { return label; }
    }

    public record StatusFilterItem(TransactionStatus status, String label) {
        public static StatusFilterItem all() { return new StatusFilterItem(null, "All"); }
        public static StatusFilterItem of(TransactionStatus s) { return new StatusFilterItem(s, s.name()); }
        @Override public String toString() { return label; }
    }

    public interface Actions {
        void onEdit(TransactionRowVM tx);
        void onDelete(TransactionRowVM tx);
        void onApprove(TransactionRowVM tx);
        void onDecline(TransactionRowVM tx);
        void onResubmit(TransactionRowVM tx);
    }

    public static final class  TransactionRowVM {
        private final SimpleStringProperty id = new SimpleStringProperty();
        private final SimpleLongProperty timestampEpoch = new SimpleLongProperty();

        private final TransactionStatus status;
        private final TransactionType type;

        private final SimpleStringProperty createdBy = new SimpleStringProperty();
        private final SimpleStringProperty initiator = new SimpleStringProperty();
        private final SimpleStringProperty target = new SimpleStringProperty();

        private final SimpleDoubleProperty amount = new SimpleDoubleProperty();

        public TransactionRowVM(String id,
                             Instant timestamp,
                             TransactionStatus status,
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
        public TransactionRowVM(TransactionEntity tx) {
            this.id.set(tx.getTxId());
            this.timestampEpoch.set(0);
            this.status = tx.getStatus();
            this.type = tx.getType();
            this.createdBy.set(tx.getCreatedBy());
            this.target.set(tx.getPayload());//TODO: расшифровать payload
            this.amount.set(tx.getPayload().length());
        }

        public String id() { return id.get(); }
        public Instant timestamp() { return Instant.ofEpochSecond(timestampEpoch.get()); }
        public TransactionStatus status() { return status; }
        public TransactionType type() { return type; }
        public String createdBy() { return createdBy.get(); }
        public String initiator() { return initiator.get(); }
        public String target() { return target.get(); }
        public double amount() { return amount.get(); }

        // for PropertyValueFactory("amount")
        public double getAmount() { return amount.get(); }

        public static TransactionRowVM demo(String id, Instant ts, TransactionStatus st, TransactionType tp,
                                         String createdBy, String initiator, String target, double amount) {
            return new TransactionRowVM(id, ts, st, tp, createdBy, initiator, target, amount);
        }
    }
}
