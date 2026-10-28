
package com.igirepay.report;

import java.time.LocalDate;

public class ReportTest {

    public static void main(String[] args) {
        ReportService reports = new ReportService();

        try {
            reports.exportTransactionsCSV();

            reports.dailySummary(
                    LocalDate.of(2026, 10, 27)
            );

            reports.customerStatement(1);

            System.out.println("\nAll report tests completed!");

        } catch (Exception e) {
            System.out.println("Report error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
