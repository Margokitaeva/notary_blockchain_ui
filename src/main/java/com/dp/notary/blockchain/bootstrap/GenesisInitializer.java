package com.dp.notary.blockchain.bootstrap;

import com.dp.notary.blockchain.blockchain.BlockchainModule;
import org.springframework.stereotype.Component;

@Component
public class GenesisInitializer {

    public GenesisInitializer(BlockchainModule module) {
        module.ensureGenesis();
    }
}
