package com.dp.notary.blockchain.auth;

import java.util.Arrays;

public enum Role {
    LEADER("Leader"),
    REPLICA("Replica");

    private final String label;

    Role(String label) {
        this.label = label;
    }

    public String displayName() {
        return label;
    }

    public static Role fromString(String value) {
        if (value == null) {
            return REPLICA;
        }

        return Arrays.stream(values())
                .filter(role -> role.label.equalsIgnoreCase(value))
                .findFirst()
                .orElse(REPLICA);
    }
}
