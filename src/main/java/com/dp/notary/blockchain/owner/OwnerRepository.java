package com.dp.notary.blockchain.owner;

import com.dp.notary.blockchain.blockchain.model.OwnerEntity;

import java.util.List;

public interface OwnerRepository {
    OwnerEntity findByName(String name);
    void updateShares(OwnerEntity owner);

    List<OwnerEntity> findAll();
    List<OwnerEntity> findAll(String filter);
    List<String> findAllOwnerNames();
}
