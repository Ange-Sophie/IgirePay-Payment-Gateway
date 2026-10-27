
package com.igirepay;

import com.igirepay.dao.AccountDAO;
import com.igirepay.dao.CustomerDAO;
import com.igirepay.model.Account;
import com.igirepay.model.Customer;
import com.igirepay.model.WalletAccount;

import java.math.BigDecimal;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        try {
            CustomerDAO customerDAO = new CustomerDAO();
            AccountDAO accountDAO = new AccountDAO();

            Customer customer = customerDAO.getAllCustomers().get(0);

            Account wallet = new WalletAccount(
                    0,
                    customer.getId(),
                    new BigDecimal("10000"),
                    "test-hash"
            );

            int accountId = accountDAO.addAccount(wallet, "WALLET");
            System.out.println("Account created with ID: " + accountId);

            Account savedAccount = accountDAO.getAccountById(accountId);
            System.out.println("Saved account: " + savedAccount);

            System.out.println("Customer accounts:");
            for (Account account :
                    accountDAO.getAccountsByCustomerId(customer.getId())) {
                System.out.println(account);
            }

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }
}
