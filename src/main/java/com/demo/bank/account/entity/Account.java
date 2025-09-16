package com.demo.bank.account.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import lombok.Getter;

@Getter
@Entity
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String firstName;
    private String lastName;
    private double balance;
    @Enumerated(EnumType.STRING)
    private Currency currency;

    public Account() {}

    public Account(String firstName, String lastName, Currency currency) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.currency = currency;
        this.balance = 0.0;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public void deposit(double amount) {
        if (amount > 0) {
            balance += amount;
        }
    }

    public boolean withdraw(double amount) {
        if (amount > 0 && amount <= balance) {
            balance -= amount;
            return true;
        }
        return false;
    }
}
