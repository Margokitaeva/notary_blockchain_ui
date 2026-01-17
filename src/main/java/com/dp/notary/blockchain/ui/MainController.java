package com.dp.notary.blockchain.ui;

import com.dp.notary.blockchain.App;
import com.dp.notary.blockchain.auth.SessionService;
import com.dp.notary.blockchain.blockchain.BlockchainService;
import com.dp.notary.blockchain.blockchain.model.TransactionType;
import com.dp.notary.blockchain.config.NotaryProperties;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.springframework.stereotype.Component;
import com.dp.notary.blockchain.auth.Role;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.function.Consumer;

@Component
public class MainController {


    // ===== HEADER =====
    @FXML private Label pageTitle;
    @FXML private Label userNameLabel;
    @FXML private Label roleLabel;

    // ===== MENU (role-based) =====
    @FXML private Button pendingBtn;     // leader only
    @FXML private Button submittedBtn;   // replica only
    @FXML private Button declinedBtn;

    // ===== CENTER PLACEHOLDER =====
    @FXML private VBox contentRoot;

    // for cancel to return list, that was before
    private TransactionsListController.Mode lastTxListMode = TransactionsListController.Mode.PENDING;

    private final BlockchainService blockchainService;
    private final SessionService sessionService;
    private final NotaryProperties props;

    public MainController(BlockchainService blockchainService, SessionService sessionService, NotaryProperties props) {
        this.blockchainService = blockchainService;
        this.sessionService = sessionService;
        this.props = props;
    }

    // ===== INIT =====
    @FXML
    private void initialize() {
        setUser(sessionService.getName(), sessionService.getRole());
        openDashboard();
    }

    // ===== PUBLIC API (потом дергать из App/Auth) =====

    public void setUser(String fullName, Role role) {

        userNameLabel.setText("User: " + fullName);
        roleLabel.setText("Role: " + role.displayName());

        if (!sessionService.isAuthenticated()){
            App.get().showLogin();
            return;
        }


        boolean isLeader = sessionService.validateRole(Role.LEADER) && props.role().equals("LEADER");
        boolean isReplica = sessionService.validateRole(Role.REPLICA);

        pendingBtn.setVisible(isLeader);
        pendingBtn.setManaged(isLeader);
        submittedBtn.setVisible(isReplica);
        submittedBtn.setManaged(isReplica);
        declinedBtn.setVisible(isReplica);
        declinedBtn.setManaged(isReplica);
    }

    public void setPageTitle(String title) {
        pageTitle.setText(title);
    }

    // ===== MENU ACTIONS =====d

    @FXML
    private void onDashboard() {
        openDashboard();
    }

    @FXML
    private void onTransactions() {
//        setPageTitle("Transactions");

        openTransactions(TransactionsListController.Mode.APPROVED);

    }

    @FXML
    private void onNewTransaction() {
//        setPageTitle("New Transaction");

        openCreateTransaction();
//        clearContent();
    }

    @FXML
    private void onDrafts() {
//        setPageTitle("Drafts");

        openTransactions(TransactionsListController.Mode.DRAFTS);

    }

    @FXML
    private void onPending() {
//        setPageTitle("Pending Transactions");

        openTransactions(TransactionsListController.Mode.PENDING);
    }

    @FXML
    private void onSubmitted() {
//        setPageTitle("My Submitted Transactions");

        openTransactions(TransactionsListController.Mode.MY_SUBMITTED);
    }

    @FXML
    private void onDeclined() {
//        setPageTitle("My Declined Transactions");

        openTransactions(TransactionsListController.Mode.DECLINED);
    }

    @FXML
    private void onLogout() {
        App.get().showLogin();
    }

    // ===== HELPERS =====

    private void clearContent() {
        if (contentRoot != null) {
            contentRoot.getChildren().clear();
        }
    }

    private void loadIntoContent(String fxmlResourcePath, Consumer<Object> controllerInit) {

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlResourcePath));
            loader.setControllerFactory(App.get().getSpringContext()::getBean);
            Node root = loader.load();

            Object controller = loader.getController();
            if (controllerInit != null && controller != null) {
                controllerInit.accept(controller);
            }

            contentRoot.getChildren().setAll(root);

        } catch (IOException e) {
            throw new RuntimeException("Failed to load view: " + fxmlResourcePath, e);
        }
    }

    private void openCreateTransaction() {
        setPageTitle("New Transaction");

        loadIntoContent("/fxml/TransactionFormView.fxml", controller -> {
            TransactionFormController f = (TransactionFormController) controller;

            f.setMode(TransactionFormController.FormMode.CREATE);

            f.setActions(new TransactionFormController.Actions() {
                @Override
                public void onCancel() {
                    // ничего не сохраняем — просто вернуться назад
                    openTransactions(lastTxListMode);
                }

                @Override
                public void onSaveDraft() {
                    openTransactions(TransactionsListController.Mode.DRAFTS);
                }

                @Override
                public void onSubmit(boolean approveImmediately) {
                    if (approveImmediately) {
                        openTransactions(TransactionsListController.Mode.APPROVED);
                    } else {
                        openTransactions(TransactionsListController.Mode.MY_SUBMITTED);
                    }
                }
            });
        });
    }


    private void openTransactions(TransactionsListController.Mode mode) {
        lastTxListMode = mode;

        setPageTitle(
                switch (mode) {
                    case APPROVED -> "Transactions";
                    case PENDING -> "Pending transactions";
                    case DRAFTS -> "Drafts";
                    case MY_SUBMITTED -> "My submitted transactions";
                    case DECLINED -> "My declined transactions";
                    default -> "Dashboard";
                }
        );

        loadIntoContent("/fxml/TransactionsListView.fxml", controller -> {
            TransactionsListController c = (TransactionsListController) controller;
            c.setMode(mode);

            c.setActions(new TransactionsListController.Actions() {
                @Override
                public void onEdit(TransactionsListController.TransactionRowVM tx) {
                    openEditTransaction(tx);
                }

                @Override
                public void onDelete() {
                    openTransactions(mode);
                }

                @Override
                public void onApprove() {
                    openTransactions(mode);
                }

                @Override
                public void onDecline() {
                    openTransactions(mode);
                }

                @Override
                public void onResubmit() {
                    openTransactions(mode);
                }
            });
        });
    }

    private void openEditTransaction(TransactionsListController.TransactionRowVM tx) {
        setPageTitle("Edit transaction");

        loadIntoContent("/fxml/TransactionFormView.fxml", controller -> {
            TransactionFormController f = (TransactionFormController) controller;

            f.setMode(TransactionFormController.FormMode.EDIT);
            // ВАЖНО:
            // Сейчас твой TransactionFormController ожидает СВОЙ TransactionVM record.
            // Самый простой путь — сделать маппинг:
            TransactionFormController.TransactionFormVM vm =
                    new TransactionFormController.TransactionFormVM(
                            tx.id(),
                            tx.timestamp(),
                            TransactionType.valueOf(tx.type().name()),
                            tx.createdBy(),
                            new BigDecimal(tx.amount()),
                            tx.target(),
                            tx.initiator()
                    );

            f.setTransaction(vm);

            f.setActions(new TransactionFormController.Actions() {
                @Override
                public void onCancel() {
                    openTransactions(lastTxListMode);
                }

                @Override
                public void onSaveDraft() {
                    // timestamp в data уже будет NOW (как ты хотела)
                    // TODO: api.updateDraftOrSaveDraft(data)
                    openTransactions(TransactionsListController.Mode.DRAFTS);
                }

                @Override
                public void onSubmit(boolean approveImmediately) {
                    if (approveImmediately) {
                        openTransactions(TransactionsListController.Mode.APPROVED);
                    } else {
                        openTransactions(TransactionsListController.Mode.MY_SUBMITTED);
                    }
                }
            });
        });
    }

    private void openDashboard() {
        setPageTitle("Dashboard");
//        clearContent(); // comment this
        // TODO: uncomment everything below when connect to existing functions
        loadIntoContent("/fxml/DashboardView.fxml", controller -> {
            DashboardController c = (DashboardController) controller;

            c.setCompany(new DashboardController.CompanyVM("H&P.Co"));

//            // TODO ledger state -> shares per owner
//            c.setSharesData(
//                    /* List<OwnerSharesVM> */,
//                    /* totalShares */
//            );
            // вынести первый иф в отдельную функцию

            if (!sessionService.isAuthenticated()){
                App.get().showLogin();
                return;
            }
            if (sessionService.validateRole(Role.LEADER)) {
                c.configureForLeader(
                        new DashboardController.LeaderStatsVM(
                                blockchainService.totalApproved(null, null, null, null),
                                blockchainService.totalSubmitted(null, null, null, null),
                                blockchainService.totalDraft(sessionService.getName(), null, null, null)
                        )
                );
            } else {
                String username = sessionService.getName();
                c.configureForReplica(
                        new DashboardController.ReplicaStatsVM(
                                blockchainService.totalApproved(username, null, null, null),
                                blockchainService.totalSubmitted(username, null, null, null),
                                blockchainService.totalDraft(username, null, null, null),
                                blockchainService.totalDeclined(username, null, null, null)
                        )
                );
            }
        });
    }

}
