package com.dp.notary.blockchain.blockchain.logic;

import com.dp.notary.blockchain.blockchain.model.Transaction;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

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
}
