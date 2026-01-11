package com.dp.notary.blockchain.auth;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@Repository
public class UserRepository {

    private final JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbc) {
        this.jdbcTemplate = jdbc;
    }

    // RowMapper для User
    private static final RowMapper<User> USER_MAPPER = new RowMapper<>() {
        @Override
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            Long id = rs.getLong("id");
            String name = rs.getString("name");
            String role = rs.getString("role");
            String hash = rs.getString("password_hash");
            return new User(id, name, role, hash);
        }
    };

    /**
     * Поиск пользователя по имени
     * hash будет пустой
     */
    public Optional<User> findByName(String name) {
        try {
            String sql = "SELECT id, name, role FROM users WHERE name = ?";
            User user = jdbcTemplate.queryForObject(sql, (rs, rowNum) ->
                    new User(
                            rs.getLong("id"),
                            rs.getString("name"),
                            rs.getString("role"),
                            ""
                    ), name);
            return Optional.of(user);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Поиск пользователя по имени и хэшу пароля
     */
    public Optional<User> findByNameAndHash(String name, String passwordHash) {
        try {
            String sql = "SELECT id, name, role, password_hash FROM users WHERE name = ? AND password_hash = ?";
            User user = jdbcTemplate.queryForObject(sql, USER_MAPPER, name, passwordHash);
            return Optional.of(user);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Сохранение пользователя
     */
    public boolean saveUser(String name, String passwordHash, String role) {
        try {
            String sql = "INSERT INTO users(name, password_hash, role) VALUES(?, ?, ?)";
            jdbcTemplate.update(sql, name, passwordHash, role);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
