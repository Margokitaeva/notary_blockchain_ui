package com.dp.notary.blockchain.ui;

import com.dp.notary.blockchain.App;
import com.dp.notary.blockchain.auth.AuthService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.springframework.stereotype.Component;
import com.dp.notary.blockchain.auth.Role;
import java.io.IOException;
import java.util.function.Consumer;

import java.time.LocalDateTime;
import java.time.ZoneId;

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

    private final AuthService authService;

    public MainController(AuthService authService) {
        this.authService = authService;
    }

    // ===== INIT =====
    @FXML
    private void initialize() {
        setUser(authService.getNameFromToken(App.get().getToken()), authService.getRoleFromToken((App.get().getToken())));
        setPageTitle("Dashboard");
    }

    // ===== PUBLIC API (потом дергать из App/Auth) =====

    public void setUser(String fullName, Role role) {

        userNameLabel.setText("User: " + fullName);
        roleLabel.setText("Role: " + role.displayName());

        pendingBtn.setVisible(role == Role.LEADER);
        submittedBtn.setVisible(role == Role.REPLICA);
    }

    public void setPageTitle(String title) {
        pageTitle.setText(title);
    }

    // ===== MENU ACTIONS =====d

    @FXML
    private void onDashboard() {
        setPageTitle("Dashboard");
        clearContent(); // comment this
        // TODO: uncomment everything below when connect to existing functions
//        loadIntoContent("/fxml/DashboardView.fxml", controller -> {
//            DashboardController c = (DashboardController) controller;
//
//            // TODO get current company (send only String name)
//            c.setCompany(new DashboardController.CompanyVM(
//                    /* company.getName() */
//            ));
//
//            // TODO ledger state -> shares per owner
//            c.setSharesData(
//                    /* List<OwnerSharesVM> */,
//                    /* totalShares */
//            );
//
//            // TODO int number of: total number transactions, pending number transactions,
//            //  drafts per user number transactions
//            if (currentRole == Role.LEADER) {
//                c.configureForLeader(
//                        new DashboardController.LeaderStatsVM(
//                                /* total */,
//                                /* pending */,
//                                /* drafts */
//                        )
//                );
//            } else {
//                // TODO int number of: total number transactions, submitted number transactions,
//                //  drafts per user number transactions, declined per user number transactions
//                c.configureForReplica(
//                        new DashboardController.ReplicaStatsVM(
//                                /* total */,
//                                /* submitted */,
//                                /* drafts */,
//                                /* declined */
//                        )
//                );
//            }
//        });

    }

    @FXML
    private void onTransactions() {
//        setPageTitle("Transactions");

        openTransactions(TransactionsListController.Mode.APPROVED);

//        loadIntoContent("/fxml/TransactionsListView.fxml", controller -> {
//            TransactionsListController c = (TransactionsListController) controller;
//            c.setMode(TransactionsListController.Mode.APPROVED);
//
//            // TODO: вместо демо — список из API:
//            // c.setItems(FXCollections.observableArrayList(api.getApprovedTransactions()));
//        });
//        clearContent();

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

//        loadIntoContent("/fxml/TransactionsListView.fxml", controller -> {
//            TransactionsListController c = (TransactionsListController) controller;
//            c.setMode(TransactionsListController.Mode.DRAFTS);
//
//            // TODO: c.setItems(FXCollections.observableArrayList(api.getDrafts()));
//        });
//        clearContent();
    }

    @FXML
    private void onPending() {
//        setPageTitle("Pending Transactions");

        openTransactions(TransactionsListController.Mode.PENDING);

//        loadIntoContent("/fxml/TransactionsListView.fxml", controller -> {
//            TransactionsListController c = (TransactionsListController) controller;
//            c.setMode(TransactionsListController.Mode.PENDING);
//
//            // TODO: c.setItems(FXCollections.observableArrayList(api.getPending()));
//        });
//        clearContent();
    }

    @FXML
    private void onSubmitted() {
//        setPageTitle("My Submitted Transactions");

        openTransactions(TransactionsListController.Mode.MY_SUBMITTED);

//        loadIntoContent("/fxml/TransactionsListView.fxml", controller -> {
//            TransactionsListController c = (TransactionsListController) controller;
//            c.setMode(TransactionsListController.Mode.MY_SUBMITTED);
//
//            // TODO: c.setItems(FXCollections.observableArrayList(api.getMySubmittedAndDeclined()));
//        });
//        clearContent();
    }

    @FXML
    private void onDeclined() {
//        setPageTitle("My Declined Transactions");

        openTransactions(TransactionsListController.Mode.DECLINED);
    }

    @FXML
    private void onLogout() {
        try {
            App.get().setToken(null);
            App.get().showLogin();
        }
        catch (IOException ignored) {}
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
            f.setRole(authService.getRoleFromToken(App.get().getToken()));
            f.setCurrentUser(authService.getNameFromToken(App.get().getToken()));

            f.setActions(new TransactionFormController.Actions() {
                @Override
                public void onCancel() {
                    // ничего не сохраняем — просто вернуться назад
                    openTransactions(lastTxListMode);
                }

                @Override
                public void onSaveDraft(TransactionFormController.TransactionPayload data) {
                    // TODO: api.saveDraft(data)
                    openTransactions(TransactionsListController.Mode.DRAFTS);
                }

                @Override
                public void onSubmit(TransactionFormController.TransactionPayload data, boolean approveImmediately) {
                    // TODO: api.submit(data, approveImmediately)
                    openTransactions(TransactionsListController.Mode.PENDING);
                }
            });
        });
    }


    private void openTransactions(TransactionsListController.Mode mode) {
        lastTxListMode = mode;

        setPageTitle(
                switch (mode) {
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
                public void onDelete(TransactionsListController.TransactionRowVM tx) {
                    // TODO: api.delete(tx.id())
                    openTransactions(mode);
                }

                @Override
                public void onApprove(TransactionsListController.TransactionRowVM tx) {
                    // TODO: api.approve(tx.id())
                    openTransactions(mode);
                }

                @Override
                public void onDecline(TransactionsListController.TransactionRowVM tx) {
                    // TODO: api.decline(tx.id())
                    openTransactions(mode);
                }

                @Override
                public void onResubmit(TransactionsListController.TransactionRowVM tx) {
                    // TODO: api.resubmit(tx.id())
                    openTransactions(mode);
                }
            });
        });
    }

    private void openEditTransaction(TransactionsListController.TransactionRowVM tx) {
        setPageTitle("Edit transaction #" + tx.id());

        loadIntoContent("/fxml/TransactionFormView.fxml", controller -> {
            TransactionFormController f = (TransactionFormController) controller;

            f.setMode(TransactionFormController.FormMode.EDIT);
            f.setRole(authService.getRoleFromToken(App.get().getToken()));
            f.setCurrentUser(authService.getNameFromToken(App.get().getToken()));

            // ВАЖНО:
            // Сейчас твой TransactionFormController ожидает СВОЙ TransactionVM record.
            // Самый простой путь — сделать маппинг:
            TransactionFormController.TransactionFormVM vm =
                    new TransactionFormController.TransactionFormVM(
                            tx.id(),
                            LocalDateTime.ofInstant(tx.timestamp(), ZoneId.systemDefault()),
                            TransactionFormController.TransactionType.valueOf(tx.type().name()),
                            tx.createdBy(),
                            tx.initiator(),
                            tx.target(),
                            (int) Math.round(tx.amount())
                    );

            f.setTransaction(vm);

            f.setActions(new TransactionFormController.Actions() {
                @Override
                public void onCancel() {
                    openTransactions(lastTxListMode);
                }

                @Override
                public void onSaveDraft(TransactionFormController.TransactionPayload data) {
                    // timestamp в data уже будет NOW (как ты хотела)
                    // TODO: api.updateDraftOrSaveDraft(data)
                    openTransactions(TransactionsListController.Mode.DRAFTS);
                }

                @Override
                public void onSubmit(TransactionFormController.TransactionPayload data, boolean approveImmediately) {
                    // TODO: api.updateAndSubmit(data, approveImmediately)
//                  // TODO: тут реальный вызов API
                    //    // если approveImmediately=true -> submit+approve
                    //    // иначе -> submit в pending
                    //
                    if (approveImmediately) {
                        // лидер: сразу approved (или куда ты показываешь approved)
                        openTransactions(TransactionsListController.Mode.APPROVED);
                        // или другой режим/вкладка если у тебя есть "Approved"
                    } else {
                        // реплика: уходит на ожидание
                        openTransactions(TransactionsListController.Mode.MY_SUBMITTED);
                    }
//                    openTransactions(TransactionsListController.Mode.PENDING);
                }
            });
        });
    }

}
