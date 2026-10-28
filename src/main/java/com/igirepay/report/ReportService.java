
package com.igirepay.report;

import com.igirepay.util.DBConnection;

import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class ReportService {

    private static final Path REPORT_FOLDER = Path.of("reports");

    public void exportTransactionsCSV() throws SQLException, IOException {
        Files.createDirectories(REPORT_FOLDER);

        String sql = """
                SELECT id, account_id, reference_id,
                       transaction_type, amount, created_at
                FROM transactions
                ORDER BY id DESC
                """;

        Path file = REPORT_FOLDER.resolve("transactions.csv");

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery();
             BufferedWriter writer = Files.newBufferedWriter(
                     file, StandardCharsets.UTF_8)) {

            writer.write("ID,Account ID,Reference,Type,Amount,Date");
            writer.newLine();

            while (result.next()) {
                writer.write(
                        result.getInt("id") + "," +
                                result.getInt("account_id") + "," +
                                csv(result.getString("reference_id")) + "," +
                                csv(result.getString("transaction_type")) + "," +
                                result.getBigDecimal("amount") + "," +
                                csv(result.getString("created_at"))
                );
                writer.newLine();
            }
        }

        System.out.println("CSV exported: " + file.toAbsolutePath());
    }

    public void dailySummary(LocalDate date) throws SQLException {
        String sql = """
                SELECT transaction_type,
                       COUNT(*) AS total_transactions,
                       SUM(amount) AS total_amount
                FROM transactions
                WHERE created_at >= ?
                  AND created_at < ?
                GROUP BY transaction_type
                ORDER BY transaction_type
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setObject(1, date.atStartOfDay());
            statement.setObject(2, date.plusDays(1).atStartOfDay());

            try (ResultSet result = statement.executeQuery()) {
                System.out.println("\n===== DAILY SUMMARY: " + date + " =====");

                boolean found = false;

                while (result.next()) {
                    found = true;
                    System.out.println(
                            result.getString("transaction_type") +
                                    " | Count: " + result.getInt("total_transactions") +
                                    " | Total: " + result.getBigDecimal("total_amount") +
                                    " RWF"
                    );
                }

                if (!found) {
                    System.out.println("No transactions found for this date.");
                }
            }
        }
    }

    public void customerStatement(int customerId)
            throws SQLException, IOException {

        String customerSql = """
                SELECT full_name FROM customers WHERE id = ?
                """;

        String transactionSql = """
                SELECT t.id, t.account_id, t.reference_id,
                       t.transaction_type, t.amount, t.created_at
                FROM transactions t
                JOIN accounts a ON t.account_id = a.id
                WHERE a.customer_id = ?
                ORDER BY t.created_at, t.id
                """;

        Files.createDirectories(REPORT_FOLDER);

        try (Connection connection = DBConnection.getConnection()) {

            String customerName;

            try (PreparedStatement statement =
                         connection.prepareStatement(customerSql)) {

                statement.setInt(1, customerId);

                try (ResultSet result = statement.executeQuery()) {
                    if (!result.next()) {
                        System.out.println("Customer not found.");
                        return;
                    }
                    customerName = result.getString("full_name");
                }
            }

            Path file = REPORT_FOLDER.resolve(
                    "customer_" + customerId + "_statement.csv");

            try (PreparedStatement statement =
                         connection.prepareStatement(transactionSql);
                 BufferedWriter writer = Files.newBufferedWriter(
                         file, StandardCharsets.UTF_8)) {

                statement.setInt(1, customerId);

                writer.write("Customer ID,Customer Name,Transaction ID," +
                        "Account ID,Reference,Type,Amount,Date");
                writer.newLine();

                BigDecimal totalDeposits = BigDecimal.ZERO;
                BigDecimal totalWithdrawals = BigDecimal.ZERO;

                try (ResultSet result = statement.executeQuery()) {
                    while (result.next()) {
                        String type = result.getString("transaction_type");
                        BigDecimal amount = result.getBigDecimal("amount");

                        if (type.equals("DEPOSIT") ||
                                type.equals("TRANSFER_IN")) {
                            totalDeposits = totalDeposits.add(amount);
                        }

                        if (type.equals("WITHDRAW") ||
                                type.equals("TRANSFER_OUT")) {
                            totalWithdrawals = totalWithdrawals.add(amount);
                        }

                        writer.write(
                                customerId + "," +
                                        csv(customerName) + "," +
                                        result.getInt("id") + "," +
                                        result.getInt("account_id") + "," +
                                        csv(result.getString("reference_id")) + "," +
                                        csv(type) + "," +
                                        amount + "," +
                                        csv(result.getString("created_at"))
                        );
                        writer.newLine();
                    }
                }

                System.out.println("\n===== CUSTOMER STATEMENT =====");
                System.out.println("Customer: " + customerName);
                System.out.println("Recorded incoming: " +
                        totalDeposits + " RWF");
                System.out.println("Recorded outgoing: " +
                        totalWithdrawals + " RWF");
                System.out.println("Statement exported: " +
                        file.toAbsolutePath());
            }
        }
    }

    private String csv(String value) {
        if (value == null) {
            return "";
        }

        String safe = value;

        if (!safe.isEmpty() &&
                "=+-@".indexOf(safe.charAt(0)) >= 0) {
            safe = "'" + safe;
        }

        return "\"" + safe.replace("\"", "\"\"") + "\"";
    }
}
