package com.dp.notary.blockchain.auth;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import org.springframework.beans.factory.annotation.Value;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;

@Repository
public class UserRepository {

    private final JdbcTemplate jdbcTemplate;
    @Value("${tokens.leaderUserName}")
    private String leader;

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
            return new User(id, name, Role.valueOf(role), hash);
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
                            Role.valueOf(rs.getString("role")),
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
            assert user != null;
            return Optional.of(user);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Сохранение пользователя
     */
    public boolean saveUser(String name, String passwordHash) {
        try {
            String sql = "INSERT INTO users(name, password_hash, role) VALUES(?, ?, ?)";
            if (Objects.equals(name, leader)){
                jdbcTemplate.update(sql, name, passwordHash, Role.valueOf("LEADER"));
            }else {
                jdbcTemplate.update(sql, name, passwordHash, Role.valueOf("REPLICA"));
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
