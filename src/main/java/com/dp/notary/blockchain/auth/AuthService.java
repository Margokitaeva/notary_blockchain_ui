package com.dp.notary.blockchain.auth;
import com.dp.notary.blockchain.ui.MainController;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final TokenProvider tokenProvider;
    public AuthService(UserRepository userRepository, TokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.tokenProvider = tokenProvider;
    }

    /**
     * Регистрация нового пользователя
     * Возвращает 0 если успешно, 1 если пользователь существует или ошибка
     */
    public boolean signUp(String name, String passwordHash) {
        Optional<User> existing = userRepository.findByName(name);
        if (existing.isPresent()) {
            return false; // пользователь уже существует
        }
        return userRepository.saveUser(name, passwordHash);

    }

    /**
     * Логин по имени и паролю, возвращает JWT
     * Если имя/пароль неверные — возвращает null
     */
    public String login(String name, String passwordHash) {
        Optional<User> userOpt = userRepository.findByNameAndHash(name, passwordHash);
        if (userOpt.isEmpty()) return "";

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
            return ""; // токен невалидный
        }
    }

    /**
     * Получить роль пользователя из токена
     */
    public Role getRoleFromToken(String token) {
        if (!tokenProvider.validateToken(token)) return null;
        return tokenProvider.getRole(token);
    }

    /**
     * Проверка токена и роли пользователя
     * Возвращает true если токен валиден и роль совпадает
     */
    public boolean validateRole(String token, Role expectedRole) {
        Role role = tokenProvider.getRole(token);
        return expectedRole.equals(role);
    }

    //refreshes if false
    public String validateToken(String token){
        if(tokenProvider.validateToken(token)){
            return token;
        }else{
            return loginWithToken(token);
        }
    }

    /**
     * Получить имя пользователя из токена
     */
    public String getNameFromToken(String token) {
        if (!tokenProvider.validateToken(token)) return null;
        return tokenProvider.getName(token);
    }
}
