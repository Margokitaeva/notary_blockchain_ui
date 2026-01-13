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


    public BlockEntity createNextBlock(List<Integer> transactions, BlockEntity head) {
        long nextHeight = head == null ? 0 : head.getHeight() + 1;
        String prevHash = head == null ? "" : head.getHash();
        Instant ts = clock.instant();
        String hash = calculateHash(nextHeight, prevHash, ts, transactions);
        return new BlockEntity(nextHeight, hash, prevHash, ts, transactions);
    }
    public boolean validateBlock(BlockEntity previous, BlockEntity current) {


        if (!previous.getHash().equals(current.getPrevHash())) {
            return false;
        }

        String expectedHash = calculateHash(
                current.getHeight(),
                current.getPrevHash(),
                current.getTimestamp(),
                current.getTransactions()
        );
        return expectedHash.equals(current.getHash());
    }
    public String calculateHash(long height, String prevHash, Instant timestamp, List<Integer> txs) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(Long.toString(height).getBytes(StandardCharsets.UTF_8));
            digest.update("|".getBytes(StandardCharsets.UTF_8));
            digest.update(prevHash == null ? new byte[0] : prevHash.getBytes(StandardCharsets.UTF_8));
            digest.update("|".getBytes(StandardCharsets.UTF_8));
            digest.update(Long.toString(timestamp.toEpochMilli()).getBytes(StandardCharsets.UTF_8));

            for (Integer tx : txs) {
                digest.update("|".getBytes(StandardCharsets.UTF_8));
                digest.update(tx.toString().getBytes(StandardCharsets.UTF_8));
            }

            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
