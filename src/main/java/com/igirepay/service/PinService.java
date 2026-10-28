
package com.igirepay.service;

import com.igirepay.util.DBConnection;
import com.igirepay.util.PinUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PinService {

    public void createPin(int accountId, String pin)
            throws SQLException {

        PinUtil.validatePin(pin);

        String sql = """
                UPDATE accounts
                SET pin_hash = ?
                WHERE id = ? AND is_active = TRUE
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, PinUtil.hashPin(pin));
            statement.setInt(2, accountId);

            if (statement.executeUpdate() == 0) {
                throw new IllegalArgumentException(
                        "Account not found or inactive.");
            }
        }
    }

    public boolean validatePin(int accountId, String pin)
            throws SQLException {

        String sql = """
                SELECT pin_hash
                FROM accounts
                WHERE id = ? AND is_active = TRUE
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, accountId);

            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new IllegalArgumentException(
                            "Account not found or inactive.");
                }

                String storedPin = result.getString("pin_hash");

                if (!PinUtil.verifyPin(pin, storedPin)) {
                    return false;
                }

                if (!storedPin.startsWith("PBKDF2$")) {
                    updateHash(connection, accountId, storedPin,
                            PinUtil.hashPin(pin));
                }

                return true;
            }
        }
    }

    public void changePin(int accountId, String oldPin, String newPin)
            throws SQLException {

        PinUtil.validatePin(newPin);

        if (!validatePin(accountId, oldPin)) {
            throw new IllegalArgumentException(
                    "Current PIN is incorrect.");
        }

        String sql = """
                UPDATE accounts
                SET pin_hash = ?
                WHERE id = ? AND is_active = TRUE
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, PinUtil.hashPin(newPin));
            statement.setInt(2, accountId);

            if (statement.executeUpdate() == 0) {
                throw new IllegalArgumentException(
                        "Account not found or inactive.");
            }
        }
    }

    private void updateHash(Connection connection, int accountId,
                            String oldValue, String newValue)
            throws SQLException {

        String sql = """
                UPDATE accounts
                SET pin_hash = ?
                WHERE id = ? AND pin_hash = ? AND is_active = TRUE
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, newValue);
            statement.setInt(2, accountId);
            statement.setString(3, oldValue);
            statement.executeUpdate();
        }
    }
}
