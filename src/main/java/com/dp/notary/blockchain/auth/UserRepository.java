package com.dp.notary.blockchain.auth;
import com.dp.notary.blockchain.Config;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserRepository {

    private final JdbcTemplate jdbcTemplate;

    public UserRepository() {
        // Подключаем SQLite, берём путь из твоего Config
        String url = Config.getInstance().getString("DBFile");

        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.sqlite.JDBC");
        dataSource.setUrl(url);

        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /**
     * Поиск роли пользователя по имени
     */
    public String findByName(String name) {
        try {
            String sql = "SELECT role FROM users WHERE name = ?";
            return jdbcTemplate.queryForObject(sql, String.class, name);
        }
        catch (Exception e) {
            return "";
        }
    }

    /**
     * Поиск роли пользователя по имени и хэшу пароля
     */
    public String findByNamePass(String name, String passwordHash) {
        try {
            String sql = "SELECT role FROM users WHERE name = ? AND password_hash = ?";
            return jdbcTemplate.queryForObject(sql, String.class, name, passwordHash);
        }
        catch (Exception e) {
            return "";
        }
    }

    /**
     * Сохранение пользователя
     */
    public int saveUser(String name, String passwordHash, String role) {
        try{
            String sql = "INSERT INTO users(name, password_hash, role) VALUES(?, ?, ?)";
            jdbcTemplate.update(sql, name, passwordHash, role);
            return 0;
        } catch (org.springframework.jdbc.UncategorizedSQLException e) {
            return 1;
        }
    }
}
