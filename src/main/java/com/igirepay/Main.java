
package com.igirepay;

import com.igirepay.model.Account;
import com.igirepay.model.WalletAccount;
import com.igirepay.model.SavingsAccount;
import java.math.BigDecimal;

public class Main {

    public static void main(String[] args) {

        Account wallet = new WalletAccount(
                1, 101, new BigDecimal("50000"), "test-hash"
        );

        Account savings = new SavingsAccount(
                2, 101, new BigDecimal("20000"), "test-hash"
        );

        wallet.deposit(new BigDecimal("10000"));
        savings.deposit(new BigDecimal("5000"));

        System.out.println("Wallet: " + wallet.getBalance());
        System.out.println("Savings: " + savings.getBalance());

        wallet.withdraw(new BigDecimal("15000"));
        savings.withdraw(new BigDecimal("5000"));

        System.out.println("After withdrawal:");
        System.out.println("Wallet: " + wallet.getBalance());
        System.out.println("Savings: " + savings.getBalance());
    }
}
