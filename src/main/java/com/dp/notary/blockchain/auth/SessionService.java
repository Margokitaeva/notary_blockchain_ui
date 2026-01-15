package com.dp.notary.blockchain.auth;

import com.dp.notary.blockchain.App;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class SessionService {

    private final AuthService authService;

    public SessionService(AuthService authService) {
        this.authService = authService;
    }

    public void logout() throws IOException {
        App.get().setToken(null);
        App.get().showLogin();
    }

    public boolean ensureAuthenticated() {
        if (authService.validateToken(App.get().getToken()) == null) {
            try {
                logout();
            } catch (IOException ignored) {}
            return false;
        }
        return true;
    }
}
