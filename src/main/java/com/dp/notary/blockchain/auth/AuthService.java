package com.dp.notary.blockchain.auth;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final TokenProvider tokenProvider;
    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.tokenProvider = new TokenProvider();
    }

    /**
     * Регистрация нового пользователя
     * Возвращает 0 если успешно, 1 если пользователь существует или ошибка
     */
    public boolean signUp(String name, String passwordHash, String role) {
        Optional<User> existing = userRepository.findByName(name);
        if (existing.isPresent()) {
            return false; // пользователь уже существует
        }
        return userRepository.saveUser(name, passwordHash, role);

    }

    /**
     * Логин по имени и паролю, возвращает JWT
     * Если имя/пароль неверные — возвращает null
     */
    public String login(String name, String passwordHash) {
        Optional<User> userOpt = userRepository.findByNameAndHash(name, passwordHash);
        if (userOpt.isEmpty()) return null;

        User user = userOpt.get();
        return tokenProvider.createToken(user.getName(), user.getRole());
    }

    /**
     * Логин по токену — проверка токена, возвращает новый токен
     * Если токен валиден — возвращает тот же токен
     * Если токен просрочен — возвращает обновлённый
     * Если токен невалидный — возвращает null
     */
    public String loginWithToken(String token) {
        if (tokenProvider.validateToken(token)) {
            return token; // токен валиден
        }

        try {
            // если просрочен — обновляем
            return tokenProvider.refreshToken(token);
        } catch (Exception e) {
            return null; // токен невалидный
        }
    }

    /**
     * Получить роль пользователя из токена
     */
    public String getRoleFromToken(String token) {
        if (!tokenProvider.validateToken(token)) return null;
        return tokenProvider.getRole(token);
    }

    /**
     * Проверка токена и роли пользователя
     * Возвращает true если токен валиден и роль совпадает
     */
    public boolean validateTokenWithRole(String token, String expectedRole) {
        if (!tokenProvider.validateToken(token)) return false;
        String role = tokenProvider.getRole(token);
        return expectedRole.equals(role);
    }

    /**
     * Получить имя пользователя из токена
     */
    public String getNameFromToken(String token) {
        if (!tokenProvider.validateToken(token)) return null;
        return tokenProvider.getName(token);
    }
}
