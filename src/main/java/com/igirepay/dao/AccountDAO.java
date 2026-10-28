
package com.igirepay.dao;

import com.igirepay.model.Account;
import com.igirepay.model.SavingsAccount;
import com.igirepay.model.WalletAccount;
import com.igirepay.util.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AccountDAO {

    public int addAccount(Account account, String accountType)
            throws SQLException {
        String sql = """
                INSERT INTO accounts
                (customer_id, account_type, balance, pin_hash)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, account.getCustomerId());
            statement.setString(2, accountType);
            statement.setBigDecimal(3, account.getBalance());
            statement.setString(4, account.getPinHash());
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        throw new SQLException("Account could not be created.");
    }

    public Account getAccountById(int accountId) throws SQLException {
        String sql = """
                SELECT id, customer_id, account_type, balance, pin_hash
                FROM accounts
                WHERE id = ? AND is_active = TRUE
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, accountId);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) return mapAccount(result);
            }
        }
        return null;
    }

    public List<Account> getAccountsByCustomerId(int customerId)
            throws SQLException {
        String sql = """
                SELECT id, customer_id, account_type, balance, pin_hash
                FROM accounts
                WHERE customer_id = ? AND is_active = TRUE
                ORDER BY id
                """;
        List<Account> accounts = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, customerId);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) accounts.add(mapAccount(result));
            }
        }
        return accounts;
    }

    public void updateBalance(int accountId, BigDecimal balance)
            throws SQLException {
        if (balance == null || balance.signum() < 0) {
            throw new IllegalArgumentException("Balance cannot be negative.");
        }

        String sql = """
                UPDATE accounts SET balance = ?
                WHERE id = ? AND is_active = TRUE
                """;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBigDecimal(1, balance);
            statement.setInt(2, accountId);
            if (statement.executeUpdate() != 1) {
                throw new SQLException("Account not found or inactive.");
            }
        }
    }

    public void deactivateAccount(int accountId) throws SQLException {
        String sql = """
                UPDATE accounts SET is_active = FALSE
                WHERE id = ? AND is_active = TRUE AND balance = 0.00
                """;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, accountId);
            if (statement.executeUpdate() != 1) {
                throw new SQLException(
                        "Account cannot be deactivated. It must exist, be active, "
                                + "and have a zero balance.");
            }
        }
    }

    public boolean isAccountActive(int accountId) throws SQLException {
        String sql = "SELECT is_active FROM accounts WHERE id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, accountId);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() && result.getBoolean("is_active");
            }
        }
    }

    private Account mapAccount(ResultSet result) throws SQLException {
        int id = result.getInt("id");
        int customerId = result.getInt("customer_id");
        String type = result.getString("account_type");
        BigDecimal balance = result.getBigDecimal("balance");
        String pinHash = result.getString("pin_hash");

        if ("WALLET".equalsIgnoreCase(type)) {
            return new WalletAccount(id, customerId, balance, pinHash);
        }
        if ("SAVINGS".equalsIgnoreCase(type)) {
            return new SavingsAccount(id, customerId, balance, pinHash);
        }
        throw new SQLException("Unknown account type: " + type);
    }
}
