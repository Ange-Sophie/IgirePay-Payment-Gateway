
package com.igirepay.dao;

import com.igirepay.model.Transaction;
import com.igirepay.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {

    public int addTransaction(Transaction transaction)
            throws SQLException {

        String sql = """
                INSERT INTO transactions
                (account_id, reference_id, transaction_type, amount)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, transaction.getAccountId());
            statement.setString(2, transaction.getReferenceId());
            statement.setString(3, transaction.getTransactionType());
            statement.setBigDecimal(4, transaction.getAmount());

            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }

        throw new SQLException("Transaction could not be saved.");
    }

    public List<Transaction> getTransactionsByAccountId(int accountId)
            throws SQLException {

        String sql = """
                SELECT id, account_id, reference_id,
                       transaction_type, amount, created_at
                FROM transactions
                WHERE account_id = ?
                ORDER BY id DESC
                """;

        List<Transaction> transactions = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, accountId);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    transactions.add(new Transaction(
                            result.getInt("id"),
                            result.getInt("account_id"),
                            result.getString("reference_id"),
                            result.getString("transaction_type"),
                            result.getBigDecimal("amount"),
                            result.getTimestamp("created_at").toLocalDateTime()
                    ));
                }
            }
        }

        return transactions;
    }
}
