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
public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    private final AuthService authService;

    public LoginController(AuthService authService) {
        this.authService = authService;
    }

    @FXML
    private void onLogin() throws IOException {
        String u = usernameField.getText() == null ? "" : usernameField.getText().trim();
        String p = passwordField.getText() == null ? "" : passwordField.getText();

        // Пока простая валидация (логика авторизации будет позже)
        if (u.isEmpty() || p.isEmpty()) {
            errorLabel.setText("Enter username and password.");
            errorLabel.setVisible(true);
            return;
        }

        // TODO: тут будет запрос к backend / проверка роли Leader/Follower
        String token = authService.login(u, p);
        if(token.isEmpty()) {
            errorLabel.setText("Invalid username or password.");
            errorLabel.setVisible(true);
        }
        else {
            String role = authService.getRoleFromToken(token);
            App.get().showMain();
        }
    }

    @FXML
    private void onGoToSignup() throws IOException {
        App.get().showSignup();
    }
}

