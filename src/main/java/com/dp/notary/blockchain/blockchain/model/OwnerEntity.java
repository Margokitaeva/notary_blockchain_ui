package com.dp.notary.blockchain.blockchain.model;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OwnerEntity {
    private String name_surname;
    private BigDecimal shares;

    public void addShares(BigDecimal amount) {
        shares = shares.add(amount);
    }

    public boolean substractShares(BigDecimal amount) {
        if (shares.compareTo(amount) < 0)
            return false;
        shares = shares.subtract(amount);
        return true;
    }
}
