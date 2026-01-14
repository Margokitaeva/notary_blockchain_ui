package com.dp.notary.blockchain.blockchain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BlockEntity {
    private long height;
    private String prevHash;
    private Instant timestamp;
    private List<String> transactions;
}
