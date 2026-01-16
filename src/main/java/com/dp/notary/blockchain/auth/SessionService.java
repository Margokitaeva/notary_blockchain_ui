package com.dp.notary.blockchain.auth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;

@Slf4j
@Component
public class SessionService {

    private final AuthService authService;
    private String token = "";
    public SessionService(AuthService authService) {
        this.authService = authService;
    }

    public boolean signUp(String name, String passwordHash){
        return authService.signUp(name, passwordHash);
    }
    public boolean login(String name, String passwordHash){
        this.token = authService.login(name, passwordHash);
        return Objects.equals(token, "");
    }

    public boolean isAuthenticated() {
        this.token = authService.validateToken(token);
        return !Objects.equals(token, "");
    }

    public String getName(){
        return authService.getNameFromToken(token);
    }
    public Role getRole(){
        return authService.getRoleFromToken(token);
    }

    public boolean validateRole(Role role){
        return isAuthenticated() && authService.validateRole(token, role);
    }
}
