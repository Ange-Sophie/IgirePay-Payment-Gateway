
package com.igirepay.model;

import java.math.BigDecimal;

public abstract class Account {

    private int id;
    private int customerId;
    private BigDecimal balance;
    private String pinHash;//contains only hashed pins

    public Account(int id, int customerId,
                   BigDecimal balance, String pinHash) {
        this.id = id;
        this.customerId = customerId;
        setBalance(balance);
        setPinHash(pinHash);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    protected void setBalance(BigDecimal balance) {
        if (balance == null || balance.signum() < 0) {
            throw new IllegalArgumentException(
                    "Balance cannot be negative or null."
            );
        }
        this.balance = balance;
    }

    public String getPinHash() {
        return pinHash;
    }

    public void setPinHash(String pinHash) {
        if (pinHash == null || pinHash.isBlank()) {
            throw new IllegalArgumentException(
                    "PIN hash is required."
            );
        }
        this.pinHash = pinHash;
    }

    public abstract void deposit(BigDecimal amount);

    public abstract void withdraw(BigDecimal amount);

    public abstract void processTransaction(
            String transactionType, BigDecimal amount);

    protected void validateAmount(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException(
                    "Amount must be greater than zero."
            );
        }
    }

    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", customerId=" + customerId +
                ", balance=" + balance +
                '}';
    }
}
