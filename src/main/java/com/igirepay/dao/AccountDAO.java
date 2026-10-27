
package com.igirepay.dao;

import com.igirepay.model.Account;
import com.igirepay.model.SavingsAccount;
import com.igirepay.model.WalletAccount;
import com.igirepay.util.DBConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
                     sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, account.getCustomerId());
            statement.setString(2, accountType.toUpperCase());
            statement.setBigDecimal(3, account.getBalance());
            statement.setString(4, account.getPinHash());

            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }

        throw new SQLException("Account creation failed: no ID returned.");
    }

    public Account getAccountById(int accountId) throws SQLException {
        String sql = """
                SELECT id, customer_id, account_type, balance, pin_hash
                FROM accounts
                WHERE id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, accountId);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return mapAccount(result);
                }
            }

        }

        return null;
    }

    public List<Account> getAccountsByCustomerId(int customerId)
            throws SQLException {

        String sql = """
                SELECT id, customer_id, account_type, balance, pin_hash
                FROM accounts
                WHERE customer_id = ?
                ORDER BY id
                """;

        List<Account> accounts = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, customerId);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    accounts.add(mapAccount(result));
                }
            }
        }

        return accounts;
    }

    public boolean updateBalance(int accountId, BigDecimal newBalance)
            throws SQLException {

        if (newBalance == null || newBalance.signum() < 0) {
            throw new IllegalArgumentException(
                    "Account balance cannot be negative.");
        }

        String sql = """
                UPDATE accounts
                SET balance = ?
                WHERE id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setBigDecimal(1, newBalance);
            statement.setInt(2, accountId);

            return statement.executeUpdate() == 1;
        }
    }

    private Account mapAccount(ResultSet result) throws SQLException {
        int id = result.getInt("id");
        int customerId = result.getInt("customer_id");
        String type = result.getString("account_type");
        BigDecimal balance = result.getBigDecimal("balance");
        String pinHash = result.getString("pin_hash");

        Account account;

        if ("WALLET".equalsIgnoreCase(type)) {
            account = new WalletAccount(id, customerId, balance, pinHash);
        } else if ("SAVINGS".equalsIgnoreCase(type)) {
            account = new SavingsAccount(id, customerId, balance, pinHash);
        } else {
            throw new SQLException("Unknown account type: " + type);
        }

        return account;
    }
}
