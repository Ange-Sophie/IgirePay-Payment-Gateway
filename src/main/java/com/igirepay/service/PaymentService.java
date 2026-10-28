
package com.igirepay.service;

import com.igirepay.util.DBConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PaymentService {

    private static final BigDecimal SAVINGS_MINIMUM =
            new BigDecimal("5000.00");
    private static final BigDecimal MAX_AMOUNT =
            new BigDecimal("9999999999999999.99");

    public void processTransaction(
            int accountId, String referenceId,
            String type, BigDecimal amount) throws SQLException {

        validateInput(referenceId, amount);

        if (!"DEPOSIT".equalsIgnoreCase(type)
                && !"WITHDRAW".equalsIgnoreCase(type)) {
            throw new IllegalArgumentException(
                    "Transaction type must be DEPOSIT or WITHDRAW.");
        }

        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);

            try {
                reserveReference(connection, referenceId);
                AccountInfo account = lockAccount(connection, accountId);

                BigDecimal newBalance =
                        "DEPOSIT".equalsIgnoreCase(type)
                                ? account.balance.add(amount)
                                : account.balance.subtract(amount);

                checkBalance(account.type, newBalance);
                updateBalance(connection, accountId, newBalance);
                saveTransaction(connection, accountId, referenceId,
                        type.toUpperCase(), amount);

                connection.commit();
                System.out.println("Transaction successful!");
                System.out.println("New balance: " + newBalance + " RWF");

            } catch (SQLException | RuntimeException exception) {
                rollback(connection, exception);
                throw exception;
            }
        }
    }

    public void transferMoney(
            int senderId, int receiverId,
            String referenceId, BigDecimal amount) throws SQLException {

        validateInput(referenceId, amount);

        if (senderId == receiverId) {
            throw new IllegalArgumentException(
                    "Sender and receiver must be different accounts.");
        }

        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);

            try {
                reserveReference(connection, referenceId);

                AccountInfo first;
                AccountInfo second;

                if (senderId < receiverId) {
                    first = lockAccount(connection, senderId);
                    second = lockAccount(connection, receiverId);
                } else {
                    first = lockAccount(connection, receiverId);
                    second = lockAccount(connection, senderId);
                }

                AccountInfo sender =
                        senderId < receiverId ? first : second;
                AccountInfo receiver =
                        receiverId < senderId ? first : second;

                BigDecimal senderBalance =
                        sender.balance.subtract(amount);
                BigDecimal receiverBalance =
                        receiver.balance.add(amount);

                checkBalance(sender.type, senderBalance);
                checkBalance(receiver.type, receiverBalance);

                updateBalance(connection, senderId, senderBalance);
                updateBalance(connection, receiverId, receiverBalance);

                saveTransaction(connection, senderId, referenceId,
                        "TRANSFER_OUT", amount);
                saveTransaction(connection, receiverId, referenceId,
                        "TRANSFER_IN", amount);

                connection.commit();
                System.out.println("Transfer successful!");
                System.out.println(
                        "Sender balance: " + senderBalance + " RWF");
                System.out.println(
                        "Receiver balance: " + receiverBalance + " RWF");

            } catch (SQLException | RuntimeException exception) {
                rollback(connection, exception);
                throw exception;
            }
        }
    }

    private AccountInfo lockAccount(
            Connection connection, int accountId) throws SQLException {

        String sql = """
                SELECT account_type, balance, is_active
                FROM accounts
                WHERE id = ?
                FOR UPDATE
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {
            statement.setInt(1, accountId);

            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new IllegalArgumentException(
                            "Account ID " + accountId + " does not exist.");
                }

                if (!result.getBoolean("is_active")) {
                    throw new IllegalArgumentException(
                            "Account ID " + accountId
                                    + " is inactive. Transactions are not allowed.");
                }

                return new AccountInfo(
                        result.getString("account_type"),
                        result.getBigDecimal("balance"));
            }
        }
    }

    private void reserveReference(
            Connection connection, String referenceId)
            throws SQLException {

        String sql =
                "INSERT INTO processed_requests (reference_id) VALUES (?)";

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {
            statement.setString(1, referenceId);
            statement.executeUpdate();

        } catch (SQLException exception) {
            if (exception.getSQLState() != null
                    && exception.getSQLState().startsWith("23")) {
                throw new IllegalArgumentException(
                        "Duplicate reference ID: " + referenceId
                                + ". Use a new reference ID.");
            }
            throw exception;
        }
    }

    private void updateBalance(
            Connection connection, int accountId,
            BigDecimal balance) throws SQLException {

        String sql = """
                UPDATE accounts
                SET balance = ?
                WHERE id = ? AND is_active = TRUE
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {
            statement.setBigDecimal(1, balance);
            statement.setInt(2, accountId);

            if (statement.executeUpdate() != 1) {
                throw new SQLException("Account balance was not updated.");
            }
        }
    }

    private void saveTransaction(
            Connection connection, int accountId,
            String referenceId, String type,
            BigDecimal amount) throws SQLException {

        String sql = """
                INSERT INTO transactions
                (account_id, reference_id, transaction_type, amount)
                VALUES (?, ?, ?, ?)
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {
            statement.setInt(1, accountId);
            statement.setString(2, referenceId);
            statement.setString(3, type);
            statement.setBigDecimal(4, amount);
            statement.executeUpdate();
        }
    }

    private void checkBalance(String accountType, BigDecimal balance) {
        if (balance.signum() < 0) {
            throw new IllegalArgumentException(
                    "Insufficient funds. Transaction rejected.");
        }

        if ("SAVINGS".equalsIgnoreCase(accountType)
                && balance.compareTo(SAVINGS_MINIMUM) < 0) {
            throw new IllegalArgumentException(
                    "Savings accounts must maintain at least 5000 RWF.");
        }
    }

    private void validateInput(String referenceId, BigDecimal amount) {
        if (referenceId == null || referenceId.isBlank()) {
            throw new IllegalArgumentException(
                    "Reference ID cannot be empty.");
        }

        if (referenceId.length() > 100) {
            throw new IllegalArgumentException(
                    "Reference ID cannot exceed 100 characters.");
        }

        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException(
                    "Amount must be greater than zero.");
        }

        if (amount.scale() > 2) {
            throw new IllegalArgumentException(
                    "Amount cannot have more than two decimal places.");
        }

        if (amount.compareTo(MAX_AMOUNT) > 0) {
            throw new IllegalArgumentException(
                    "Amount exceeds the permitted limit.");
        }
    }

    private void rollback(
            Connection connection, Exception originalException) {
        try {
            connection.rollback();
        } catch (SQLException rollbackException) {
            originalException.addSuppressed(rollbackException);
        }
    }

    private static class AccountInfo {
        private final String type;
        private final BigDecimal balance;

        private AccountInfo(String type, BigDecimal balance) {
            this.type = type;
            this.balance = balance;
        }
    }
}
