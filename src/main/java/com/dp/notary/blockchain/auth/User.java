package com.dp.notary.blockchain.auth;

import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class User {

    private long id;

    private String name;
    private Role role;
    private String hash;
}
