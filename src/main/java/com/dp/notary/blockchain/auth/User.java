package com.dp.notary.blockchain.auth;

public class User {

    private String name;
    private String role;

    public User() {
        // пустой конструктор для Spring / Jackson
    }

    public User(String name, String role) {
        this.name = name;
        this.role = role;
    }

    // --- Геттеры ---
    public String getName() {
        return name;
    }

    public String getRole() {
        return role;
    }

    // --- Сеттеры ---
    public void setName(String name) {
        this.name = name;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "User{name='" + name + "', role='" + role + "'}";
    }
}
