
package com.igirepay;

import com.igirepay.dao.CustomerDAO;
import com.igirepay.model.Customer;
import com.igirepay.util.DBConnection;

import java.sql.Connection;
import java.sql.SQLException;

public class Main {

    public static void main(String[] args) {

        try (Connection connection =
                     DBConnection.getConnection()) {

            System.out.println("Database connected successfully!");

            CustomerDAO customerDAO = new CustomerDAO();

            Customer customer = new Customer(
                    0,
                    "Sonia Habonimana",
                    "sonia.test@example.com",
                    "0780000000"
            );

            int id = customerDAO.addCustomer(customer);

            System.out.println("Customer registered with ID: " + id);

            System.out.println("Registered customers:");

            for (Customer c : customerDAO.getAllCustomers()) {
                System.out.println(c);
            }

        } catch (SQLException e) {
            System.err.println(
                    "Database operation failed: " + e.getMessage()
            );
        }
    }
}
