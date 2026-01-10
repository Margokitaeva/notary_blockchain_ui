package com.dp.notary.blockchain.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.function.Consumer;

public class MainController {

    // ===== HEADER =====
    @FXML private Label pageTitle;
    @FXML private Label userNameLabel;
    @FXML private Label roleLabel;

    // ===== MENU (role-based) =====
    @FXML private Button pendingBtn;     // leader only
    @FXML private Button submittedBtn;   // replica only

    // ===== CENTER PLACEHOLDER =====
    @FXML private VBox contentRoot;

    // ===== INIT =====
    @FXML
    private void initialize() {
        // демо-данные, чтобы UI выглядел живым
        setUser("Name Surname", Role.LEADER);
        setPageTitle("Dashboard");
    }

    // ===== PUBLIC API (потом дергать из App/Auth) =====

    public void setUser(String fullName, Role role) {
        userNameLabel.setText("User: " + fullName);
        roleLabel.setText("Role: " + role.displayName());

        // показываем/скрываем пункты меню
        pendingBtn.setVisible(role == Role.LEADER);
        submittedBtn.setVisible(role == Role.REPLICA);
    }

    public void setPageTitle(String title) {
        pageTitle.setText(title);
    }

    // ===== MENU ACTIONS =====

    @FXML
    private void onDashboard() {
        setPageTitle("Dashboard");
        clearContent();
    }

    @FXML
    private void onTransactions() {
        setPageTitle("Transactions");

        loadIntoContent("/fxml/TransactionsListView.fxml", controller -> {
            TransactionsListController c = (TransactionsListController) controller;
            c.setMode(TransactionsListController.Mode.APPROVED);

            // TODO: вместо демо — список из API:
            // c.setItems(FXCollections.observableArrayList(api.getApprovedTransactions()));
        });
//        clearContent();
    }

    @FXML
    private void onNewTransaction() {
        setPageTitle("New Transaction");
        clearContent();
    }

    @FXML
    private void onDrafts() {
        setPageTitle("Drafts");

        loadIntoContent("/fxml/TransactionsListView.fxml", controller -> {
            TransactionsListController c = (TransactionsListController) controller;
            c.setMode(TransactionsListController.Mode.DRAFTS);

            // TODO: c.setItems(FXCollections.observableArrayList(api.getDrafts()));
        });
//        clearContent();
    }

    @FXML
    private void onPending() {
        setPageTitle("Pending Transactions");

        loadIntoContent("/fxml/TransactionsListView.fxml", controller -> {
            TransactionsListController c = (TransactionsListController) controller;
            c.setMode(TransactionsListController.Mode.PENDING);

            // TODO: c.setItems(FXCollections.observableArrayList(api.getPending()));
        });
//        clearContent();
    }

    @FXML
    private void onSubmitted() {
        setPageTitle("My Submitted Transactions");

        loadIntoContent("/fxml/TransactionsListView.fxml", controller -> {
            TransactionsListController c = (TransactionsListController) controller;
            c.setMode(TransactionsListController.Mode.MY_SUBMITTED);

            // TODO: c.setItems(FXCollections.observableArrayList(api.getMySubmittedAndDeclined()));
        });
//        clearContent();
    }

    @FXML
    private void onLogout() {
        // позже: App.showLogin()
        System.out.println("Logout clicked");
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

    // ===== ROLE ENUM =====
    public enum Role {
        LEADER("Leader"),
        REPLICA("Replica");

        private final String label;

        Role(String label) {
            this.label = label;
        }

        public String displayName() {
            return label;
        }
    }
}
