
package com.igirepay.model;

import java.math.BigDecimal;

public class SavingsAccount extends Account {

    private static final BigDecimal MINIMUM_BALANCE =
            new BigDecimal("5000");

    public SavingsAccount(int id, int customerId,
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

        BigDecimal remainingBalance =
                getBalance().subtract(amount);

        if (remainingBalance.compareTo(MINIMUM_BALANCE) < 0) {
            throw new IllegalArgumentException(
                    "Savings must retain at least 5000 RWF."
            );
        }

        setBalance(remainingBalance);
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
        return "SavingsAccount{" +
                "id=" + getId() +
                ", balance=" + getBalance() +
                '}';
    }
}
