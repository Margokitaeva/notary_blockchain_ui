package com.dp.notary.blockchain.core;

import com.dp.notary.blockchain.domain.Transaction;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PendingPool {
    private final List<Transaction> txs = new ArrayList<>();

    public synchronized void add(Transaction tx) { txs.add(tx); }

    public synchronized int size() { return txs.size(); }

    public synchronized List<Transaction> snapshot() { return List.copyOf(txs); }
}
