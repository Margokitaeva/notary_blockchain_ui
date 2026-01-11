package com.dp.notary.blockchain.auth;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
@Entity
public class User {

    @Id
    private long id;

    private String name;
    private String role;
    private String hash;

    // Пустой конструктор
    public User() {
    }

    // Конструктор со всеми полями
    public User(long id,String name, String role, String hash) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.hash = hash;
    }

    // Геттеры
    public String getName() {
        return name;
    }

    public String getRole() {
        return role;
    }

    public String getHash() {
        return hash;
    }
    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }

    // Сеттеры
    public void setName(String name) {
        this.name = name;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    // toString
    @Override
    public String toString() {
        return "User{name='" + name + "', role='" + role + "'}";
    }
}
