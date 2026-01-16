package com.dp.notary.blockchain.ui;

import com.dp.notary.blockchain.App;
import com.dp.notary.blockchain.auth.SessionService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.springframework.stereotype.Component;


@Component
public class SignupController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmField;
    @FXML private Label errorLabel;

    private final SessionService sessionService;

    SignupController(
            final SessionService sessionService
    ){
        this.sessionService = sessionService;
    }
    @FXML
    private void onCreateAccount() {
        String u = usernameField.getText() == null ? "" : usernameField.getText().trim();
        String p1 = passwordField.getText() == null ? "" : passwordField.getText();
        String p2 = confirmField.getText() == null ? "" : confirmField.getText();

        if (u.isEmpty() || p1.isEmpty() || p2.isEmpty()) {
            errorLabel.setText("Fill all fields.");
            errorLabel.setVisible(true);
            return;
        }

        if (!p1.equals(p2)) {
            errorLabel.setText("Passwords do not match.");
            errorLabel.setVisible(true);
            return;
        }

        boolean ok = sessionService.signUp(u, p1);
        if(!ok){
            errorLabel.setText("Sign up failed.");
            errorLabel.setVisible(true);
        }else {
            App.get().showLogin();
        }
    }

    @FXML
    private void onBackToLogin() {
        App.get().showLogin();
    }
}
