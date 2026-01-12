package com.dp.notary.blockchain.blockchain.logic;

import com.dp.notary.blockchain.blockchain.model.Block;
import com.dp.notary.blockchain.blockchain.model.Company;
import com.dp.notary.blockchain.blockchain.model.Transaction;
import com.dp.notary.blockchain.blockchain.model.Owner;
import com.dp.notary.blockchain.blockchain.model.TransactionStatus;
import com.dp.notary.blockchain.blockchain.model.TransactionType;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;

@Component
public class BlockProcessor {

    private final Clock clock;

    public BlockProcessor() {
        this(Clock.systemUTC());
    }

    BlockProcessor(Clock clock) {
        this.clock = clock;
    }

    public Block createGenesisBlock() {
        Transaction genesisTx = new Transaction(
                "GENESIS_TX",
                TransactionType.GRANT,
                "{}",
                "system",
                TransactionStatus.APPROVED,
                new Company("GENESIS", "Genesis Company"),
                new Owner("system", "Genesis", "Owner"),
                java.math.BigDecimal.ZERO,
                clock.instant(),
                "GENESIS_TARGET"
        );
        return createNextBlock(List.of(genesisTx), null);
    }

    public Block createNextBlock(List<Transaction> transactions, Block head) {
        long nextHeight = head == null ? 0 : head.height() + 1;
        String prevHash = head == null ? null : head.hash();
        Instant ts = clock.instant();
        String hash = calculateHash(nextHeight, prevHash, ts, transactions);
        return new Block(nextHeight, hash, prevHash, ts, transactions);
    }

    public void validateNewBlock(Block newBlock, Block previous) {
        if (newBlock.height() == 0) {
            validateGenesis(newBlock);
            return;
        }

        if (previous == null) {
            throw new IllegalStateException("Previous block is required to validate non-genesis block");
        }

        if (newBlock.height() != previous.height() + 1) {
            throw new IllegalStateException("Block height does not follow previous block");
        }

        if (!Objects.equals(newBlock.prevHash(), previous.hash())) {
            throw new IllegalStateException("Previous hash does not match chain head");
        }

        String expectedHash = calculateHash(
                newBlock.height(),
                newBlock.prevHash(),
                newBlock.timestamp(),
                newBlock.transactions()
        );
        if (!Objects.equals(expectedHash, newBlock.hash())) {
            throw new IllegalStateException("Block hash does not match calculated value");
        }
    }

    public String calculateHash(long height, String prevHash, Instant timestamp, List<Transaction> txs) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(Long.toString(height).getBytes(StandardCharsets.UTF_8));
            digest.update("|".getBytes(StandardCharsets.UTF_8));
            digest.update(prevHash == null ? new byte[0] : prevHash.getBytes(StandardCharsets.UTF_8));
            digest.update("|".getBytes(StandardCharsets.UTF_8));
            digest.update(Long.toString(timestamp.toEpochMilli()).getBytes(StandardCharsets.UTF_8));

            for (Transaction tx : txs) {
                digest.update("|".getBytes(StandardCharsets.UTF_8));
                digest.update(tx.txId().getBytes(StandardCharsets.UTF_8));
                digest.update(":".getBytes(StandardCharsets.UTF_8));
                String typeValue = tx.type() == null ? "UNKNOWN" : tx.type().name();
                digest.update(typeValue.getBytes(StandardCharsets.UTF_8));
                digest.update(":".getBytes(StandardCharsets.UTF_8));
                digest.update(tx.payload().getBytes(StandardCharsets.UTF_8));
                digest.update(":".getBytes(StandardCharsets.UTF_8));
                digest.update(tx.createdBy() == null ? new byte[0] : tx.createdBy().getBytes(StandardCharsets.UTF_8));
                digest.update(":".getBytes(StandardCharsets.UTF_8));
                String statusValue = tx.status() == null ? "UNKNOWN" : tx.status().name();
                digest.update(statusValue.getBytes(StandardCharsets.UTF_8));

                String amountValue = tx.amount() == null ? "0" : tx.amount().toPlainString();
                digest.update(":".getBytes(StandardCharsets.UTF_8));
                digest.update(amountValue.getBytes(StandardCharsets.UTF_8));

                String txTs = tx.timestamp() == null ? "0" : Long.toString(tx.timestamp().toEpochMilli());
                digest.update(":".getBytes(StandardCharsets.UTF_8));
                digest.update(txTs.getBytes(StandardCharsets.UTF_8));

                String target = tx.target() == null ? "" : tx.target();
                digest.update(":".getBytes(StandardCharsets.UTF_8));
                digest.update(target.getBytes(StandardCharsets.UTF_8));

                Owner owner = tx.owner();
                digest.update(":".getBytes(StandardCharsets.UTF_8));
                if (owner != null) {
                    digest.update(owner.id() == null ? new byte[0] : owner.id().getBytes(StandardCharsets.UTF_8));
                    digest.update(":".getBytes(StandardCharsets.UTF_8));
                    digest.update(owner.name() == null ? new byte[0] : owner.name().getBytes(StandardCharsets.UTF_8));
                    digest.update(":".getBytes(StandardCharsets.UTF_8));
                    digest.update(owner.surname() == null ? new byte[0] : owner.surname().getBytes(StandardCharsets.UTF_8));
                }

                Company company = tx.company();
                digest.update(":".getBytes(StandardCharsets.UTF_8));
                if (company != null) {
                    digest.update(company.id() == null ? new byte[0] : company.id().getBytes(StandardCharsets.UTF_8));
                    digest.update(":".getBytes(StandardCharsets.UTF_8));
                    digest.update(company.name() == null ? new byte[0] : company.name().getBytes(StandardCharsets.UTF_8));
                }
            }

            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private void validateGenesis(Block block) {
        if (block.height() != 0) {
            throw new IllegalStateException("Genesis block must start at height 0");
        }
        if (block.prevHash() != null) {
            throw new IllegalStateException("Genesis block must not have previous hash");
        }
        if (block.transactions().isEmpty()) {
            throw new IllegalStateException("Genesis block must contain at least one transaction");
        }

        String expectedHash = calculateHash(block.height(), block.prevHash(), block.timestamp(), block.transactions());
        if (!Objects.equals(expectedHash, block.hash()) && !"GENESIS".equals(block.hash())) {
            throw new IllegalStateException("Genesis hash is invalid");
        }
    }
}
