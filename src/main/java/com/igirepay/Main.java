
package com.igirepay;

import com.igirepay.model.Transaction;
import com.igirepay.service.TransactionManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Main {

    public static void main(String[] args) {

        TransactionManager manager = new TransactionManager();

        Transaction first = new Transaction(
                1,
                101,
                "PAY-001",
                "DEPOSIT",
                new BigDecimal("10000"),
                LocalDateTime.now()
        );

        Transaction duplicate = new Transaction(
                2,
                101,
                "PAY-001",
                "DEPOSIT",
                new BigDecimal("10000"),
                LocalDateTime.now()
        );

        System.out.println(
                "First request: " + manager.recordTransaction(first)
        );

        System.out.println(
                "Duplicate request: " + manager.recordTransaction(duplicate)
        );

        System.out.println(
                "Transactions saved: " +
                        manager.getTransactions().size()
        );
    }
}
