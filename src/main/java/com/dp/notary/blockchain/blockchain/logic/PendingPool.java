package com.dp.notary.blockchain.blockchain.logic;

import com.dp.notary.blockchain.blockchain.model.Transaction;
import com.dp.notary.blockchain.blockchain.model.TransactionStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class PendingPool {
    private final List<Transaction> txs = new ArrayList<>();

    public synchronized void add(Transaction tx) {
        txs.add(tx);
    }

    public synchronized int size() {
        return txs.size();
    }

    public synchronized List<Transaction> snapshot() {
        return List.copyOf(txs);
    }

    public synchronized List<Transaction> drainAll() {
        List<Transaction> copy = new ArrayList<>(txs);
        txs.clear();
        return copy;
    }

    public synchronized boolean updateStatus(String txId, TransactionStatus status) {
        for (int i = 0; i < txs.size(); i++) {
            Transaction tx = txs.get(i);
            if (tx.txId().equals(txId)) {
                txs.set(i, withStatus(tx, status));
                return true;
            }
        }
        return false;
    }

    public synchronized Optional<Transaction> removeById(String txId) {
        for (int i = 0; i < txs.size(); i++) {
            Transaction tx = txs.get(i);
            if (tx.txId().equals(txId)) {
                txs.remove(i);
                return Optional.of(tx);
            }
        }
        return Optional.empty();
    }

    private Transaction withStatus(Transaction tx, TransactionStatus status) {
        return new Transaction(
                tx.txId(),
                tx.type(),
                tx.payload(),
                tx.createdBy(),
                status,
                tx.company()
        );
    }
}
