
package com.igirepay.model;

import java.math.BigDecimal;

public class WalletAccount extends Account {

    public WalletAccount(int id, int customerId,
                         BigDecimal balance, String pinHash) {
        super(id, customerId, balance, pinHash);
    }

    @Override
    public void deposit(BigDecimal amount) {
        validateAmount(amount);
        setBalance(getBalance().add(amount));
    }

    @Override
    public void withdraw(BigDecimal amount) {
        validateAmount(amount);

        if (getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException(
                    "Insufficient wallet balance."
            );
        }

        setBalance(getBalance().subtract(amount));
    }

    @Override
    public void processTransaction(
            String transactionType, BigDecimal amount) {

        if (transactionType == null) {
            throw new IllegalArgumentException(
                    "Transaction type is required."
            );
        }

        switch (transactionType.toUpperCase()) {
            case "DEPOSIT" -> deposit(amount);
            case "WITHDRAW" -> withdraw(amount);
            default -> throw new IllegalArgumentException(
                    "Unsupported transaction type."
            );
        }
    }

    @Override
    public String toString() {
        return "WalletAccount{" +
                "id=" + getId() +
                ", balance=" + getBalance() +
                '}';
    }
}
