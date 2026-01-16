package com.dp.notary.blockchain.ui;

import com.dp.notary.blockchain.App;
import com.dp.notary.blockchain.auth.SessionService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.springframework.stereotype.Component;


@Component
public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    private final SessionService sessionService;

    public LoginController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @FXML
    private void onLogin() {
        String u = usernameField.getText() == null ? "" : usernameField.getText().trim();
        String p = passwordField.getText() == null ? "" : passwordField.getText();

        // Пока простая валидация (логика авторизации будет позже)
        if (u.isEmpty() || p.isEmpty()) {
            errorLabel.setText("Enter username and password.");
            errorLabel.setVisible(true);
            return;
        }

        boolean ok = sessionService.login(u, p);
        if(ok) {
            errorLabel.setText("Invalid username or password.");
            errorLabel.setVisible(true);
        }
        else {
            App.get().showMain();
        }
    }

    @FXML
    private void onGoToSignup() {
        App.get().showSignup();
    }
}

