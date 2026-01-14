package com.dp.notary.blockchain.blockchain;

import com.dp.notary.blockchain.blockchain.model.BlockEntity;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;

@Component
public class BlockProcessor {

    private final Clock clock;

    public BlockProcessor() {
        this(Clock.systemUTC());
    }

    BlockProcessor(Clock clock) {
        this.clock = clock;
    }


    public BlockEntity createNextBlock(List<String> transactions, BlockEntity head) {
        long nextHeight = head == null ? 0 : head.getHeight() + 1;
        String prevHash = head == null ? "" : calculateHash(head);
        Instant ts = clock.instant();
        return new BlockEntity(nextHeight, prevHash, ts, transactions);
    }
    public boolean validateBlock(BlockEntity previous, BlockEntity current) {

        String expectedHash = calculateHash(previous);


        return previous.equals(current.getPrevHash());
    }
    public String calculateHash(BlockEntity block) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(Long.toString(block.getHeight()).getBytes(StandardCharsets.UTF_8));
            digest.update("|".getBytes(StandardCharsets.UTF_8));
            digest.update(block.getPrevHash() == null ? new byte[0] : block.getPrevHash().getBytes(StandardCharsets.UTF_8));
            digest.update("|".getBytes(StandardCharsets.UTF_8));
            digest.update(Long.toString(block.getTimestamp().toEpochMilli()).getBytes(StandardCharsets.UTF_8));

            for (String tx : block.getTransactions()) {
                digest.update("|".getBytes(StandardCharsets.UTF_8));
                digest.update(tx.getBytes(StandardCharsets.UTF_8));
            }

            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
