
package com.igirepay;

import com.igirepay.dao.AccountDAO;
import com.igirepay.model.Account;
import com.igirepay.service.PaymentService;

import java.math.BigDecimal;
import java.sql.SQLException;

public class Main {

    public static void main(String[] args) {

        PaymentService paymentService = new PaymentService();
        AccountDAO accountDAO = new AccountDAO();

        String reference = "SONIA-DUPLICATE-TEST-001";

        for (int i = 1; i <= 2; i++) {
            try {
                paymentService.processTransaction(
                        1,
                        reference,
                        "DEPOSIT",
                        new BigDecimal("100")
                );

                System.out.println("Attempt " + i + ": Payment successful.");

            } catch (SQLException | RuntimeException e) {
                System.out.println(
                        "Attempt " + i + ": Payment rejected - "
                                + e.getMessage()
                );
            }
        }

        try {
            Account account = accountDAO.getAccountById(1);
            System.out.println("Final account: " + account);
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }
}
