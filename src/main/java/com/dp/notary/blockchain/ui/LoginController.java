package com.dp.notary.blockchain.ui;

import com.dp.notary.blockchain.App;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

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
        App.get().showMain();
    }

    @FXML
    private void onGoToSignup() throws IOException {
        App.get().showSignup();
    }
}

