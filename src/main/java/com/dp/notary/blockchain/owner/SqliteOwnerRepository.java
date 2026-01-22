package com.dp.notary.blockchain.owner;

import com.dp.notary.blockchain.blockchain.model.OwnerEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class SqliteOwnerRepository implements OwnerRepository {

    private final JdbcTemplate jdbc;

    public SqliteOwnerRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public OwnerEntity findById(String id) {
        return jdbc.queryForObject(
                "SELECT name_surname, shares, locked_shares FROM owners WHERE name_surname = ?",
                this::mapOwner,
                id
        );
    }

    @Override
    public void updateBalances(OwnerEntity owner) {
        jdbc.update(
                "UPDATE owners SET shares = ?, locked_shares = ? WHERE name_surname = ?",
                owner.getShares().toPlainString(),
                owner.getLockedShares().toPlainString(),
                owner.getName_surname()
        );
    }

    @Override
    public List<OwnerEntity> findAll() {
        return jdbc.query(
                "SELECT name_surname, shares, locked_shares FROM owners",
                this::mapOwner
        );
    }

    @Override
    public List<OwnerEntity> findAll(String filter) {
        return jdbc.query(
                "SELECT name_surname, shares, locked_shares FROM owners WHERE ? is NULL OR name_surname LIKE ?",
                this::mapOwner,
                filter,
                "%" + filter + "%"
        );
    }

    @Override
    public List<String> findAllOwnerNames() {
        return jdbc.query(
                "SELECT name_surname FROM owners",
                (rs, rowNum) -> rs.getString("name_surname")
        );
    }

    private OwnerEntity mapOwner(ResultSet rs, int rowNum) throws SQLException {
        return new OwnerEntity(
                rs.getString("name_surname"),
                new BigDecimal(rs.getString("shares")),
                new BigDecimal(rs.getString("locked_shares"))
        );
    }

}
