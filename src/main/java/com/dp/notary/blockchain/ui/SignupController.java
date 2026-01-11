package com.dp.notary.blockchain.ui;

import com.dp.notary.blockchain.App;
import com.dp.notary.blockchain.auth.AuthService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.springframework.stereotype.Component;

import java.io.IOException;
@Component
public class SignupController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmField;
    @FXML private Label errorLabel;

    private final AuthService authService;

    SignupController(
            final AuthService authService
    ){
        this.authService = authService;
    }
    @FXML
    private void onCreateAccount() throws IOException {
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

        // TODO: тут будет создание аккаунта через backend
        boolean ok = authService.signUp(u, p1,"Replica");
        if (ok){
            String token = authService.login(u,p1);
            System.out.println("Token: " + token);
            token = authService.loginWithToken(token);
            System.out.println("Token: " + token);
            String name = authService.getNameFromToken(token);
            System.out.println("Name: " + name);
            String role = authService.getRoleFromToken(token);
            System.out.println("Role: " + role);
            System.out.println(authService.validateTokenWithRole(token, role));
            System.out.println(authService.validateTokenWithRole(token, "hjk"));

        }
        App.get().showLogin();
    }

    @FXML
    private void onBackToLogin() throws IOException {
        App.get().showLogin();
    }
}
