package com.dp.notary.blockchain.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

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
        clearContent();
    }

    @FXML
    private void onNewTransaction() {
        setPageTitle("New Transaction");
        clearContent();
    }

    @FXML
    private void onDrafts() {
        setPageTitle("Drafts");
        clearContent();
    }

    @FXML
    private void onPending() {
        setPageTitle("Pending Transactions");
        clearContent();
    }

    @FXML
    private void onSubmitted() {
        setPageTitle("My Submitted Transactions");
        clearContent();
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
