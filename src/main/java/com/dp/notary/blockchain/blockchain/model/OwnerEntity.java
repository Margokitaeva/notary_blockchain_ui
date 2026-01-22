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
    private BigDecimal lockedShares;

    public void addShares(BigDecimal amount) {
        shares = shares.add(amount);
    }

//    public boolean substractShares(BigDecimal amount) {
//        if (shares.compareTo(amount) < 0)
//            return false;
//        shares = shares.subtract(amount);
//        return true;
//    }

    public void lockShares(BigDecimal amount) {
        if (shares.compareTo(amount) < 0) {
            throw new IllegalStateException("Not enough shares to lock");
        }
        shares = shares.subtract(amount);
        lockedShares = lockedShares.add(amount);
    }

    public void unlockShares(BigDecimal amount) {
        if (lockedShares.compareTo(amount) < 0) {
            throw new IllegalStateException("Not enough locked shares");
        }
        lockedShares = lockedShares.subtract(amount);
        shares = shares.add(amount);
    }

    public void consumeLockedShares(BigDecimal amount) {
        if (lockedShares.compareTo(amount) < 0) {
            throw new IllegalStateException("Not enough locked shares");
        }
        lockedShares = lockedShares.subtract(amount);
    }
}
