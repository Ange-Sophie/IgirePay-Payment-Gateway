
package com.igirepay.service;

import com.igirepay.util.DBConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PaymentService {

    public void processTransaction(int accountId, String referenceId,
                                   String type, BigDecimal amount)
            throws SQLException {

        if (referenceId == null || referenceId.isBlank()) {
            throw new IllegalArgumentException("Reference ID is required.");
        }

        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Amount must be positive.");
        }

        if (type == null ||
                !(type.equalsIgnoreCase("DEPOSIT")
                        || type.equalsIgnoreCase("WITHDRAW"))) {
            throw new IllegalArgumentException("Invalid transaction type.");
        }

        type = type.toUpperCase();

        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);

            try {
                // Check whether this reference was already processed.
                String checkSql =
                        "SELECT id FROM processed_requests WHERE reference_id = ?";

                try (PreparedStatement statement =
                             connection.prepareStatement(checkSql)) {
                    statement.setString(1, referenceId);

                    try (ResultSet result = statement.executeQuery()) {
                        if (result.next()) {
                            throw new IllegalArgumentException(
                                    "This reference ID was already used.");
                        }
                    }
                }

                // Lock the account while processing the payment.
                String accountSql =
                        "SELECT account_type, balance FROM accounts " +
                                "WHERE id = ? FOR UPDATE";

                String accountType;
                BigDecimal balance;

                try (PreparedStatement statement =
                             connection.prepareStatement(accountSql)) {
                    statement.setInt(1, accountId);

                    try (ResultSet result = statement.executeQuery()) {
                        if (!result.next()) {
                            throw new IllegalArgumentException(
                                    "Account not found.");
                        }

                        accountType = result.getString("account_type");
                        balance = result.getBigDecimal("balance");
                    }
                }

                BigDecimal newBalance;

                if (type.equals("DEPOSIT")) {
                    newBalance = balance.add(amount);
                } else {
                    newBalance = balance.subtract(amount);

                    if (newBalance.signum() < 0) {
                        throw new IllegalArgumentException(
                                "Insufficient balance.");
                    }

                    if (accountType.equalsIgnoreCase("SAVINGS")
                            && newBalance.compareTo(
                            new BigDecimal("5000")) < 0) {
                        throw new IllegalArgumentException(
                                "Savings must retain 5000 RWF.");
                    }
                }

                // Update the account balance.
                String updateSql =
                        "UPDATE accounts SET balance = ? WHERE id = ?";

                try (PreparedStatement statement =
                             connection.prepareStatement(updateSql)) {
                    statement.setBigDecimal(1, newBalance);
                    statement.setInt(2, accountId);
                    statement.executeUpdate();
                }

                // Save the transaction record.
                String transactionSql =
                        "INSERT INTO transactions " +
                                "(account_id, reference_id, transaction_type, amount) " +
                                "VALUES (?, ?, ?, ?)";

                try (PreparedStatement statement =
                             connection.prepareStatement(transactionSql)) {
                    statement.setInt(1, accountId);
                    statement.setString(2, referenceId);
                    statement.setString(3, type);
                    statement.setBigDecimal(4, amount);
                    statement.executeUpdate();
                }

                // Reserve the reference ID to prevent reuse.
                String requestSql =
                        "INSERT INTO processed_requests (reference_id) VALUES (?)";

                try (PreparedStatement statement =
                             connection.prepareStatement(requestSql)) {
                    statement.setString(1, referenceId);
                    statement.executeUpdate();
                }

                connection.commit();
                System.out.println("Transaction successful!");
                System.out.println("New balance: " + newBalance + " RWF");

            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            }
        }
    }
}
