
package com.igirepay;

import com.igirepay.dao.AccountDAO;
import com.igirepay.dao.CustomerDAO;
import com.igirepay.dao.TransactionDAO;
import com.igirepay.model.Account;
import com.igirepay.model.Customer;
import com.igirepay.model.SavingsAccount;
import com.igirepay.model.Transaction;
import com.igirepay.model.WalletAccount;
import com.igirepay.service.PaymentService;
import com.igirepay.service.PinService;
import com.igirepay.util.PinUtil;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    private static final CustomerDAO customerDAO = new CustomerDAO();
    private static final AccountDAO accountDAO = new AccountDAO();
    private static final TransactionDAO transactionDAO = new TransactionDAO();
    private static final PaymentService paymentService = new PaymentService();
    private static final PinService pinService = new PinService();

    public static void main(String[] args) {
        boolean running = true;

        while (running) {
            System.out.println("\n===== IGIREPAY PAYMENT GATEWAY =====");
            System.out.println("1. Register customer");
            System.out.println("2. Update customer information");
            System.out.println("3. View all customers");
            System.out.println("4. View customer accounts");
            System.out.println("5. View account balance");
            System.out.println("6. Create wallet account");
            System.out.println("7. Create savings account");
            System.out.println("8. Deposit money");
            System.out.println("9. Withdraw money");
            System.out.println("10. View transaction history");
            System.out.println("11. Transfer money");
            System.out.println("12. Deactivate an account");
            System.out.println("13. Validate account PIN");
            System.out.println("14. Change account PIN");
            System.out.println("0. Exit");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1" -> registerCustomer();
                    case "2" -> updateCustomer();
                    case "3" -> viewCustomers();
                    case "4" -> viewCustomerAccounts();
                    case "5" -> viewAccountBalance();
                    case "6" -> createAccount("WALLET");
                    case "7" -> createAccount("SAVINGS");
                    case "8" -> makePayment("DEPOSIT");
                    case "9" -> makePayment("WITHDRAW");
                    case "10" -> viewTransactionHistory();
                    case "11" -> transferMoney();
                    case "12" -> deactivateAccount();
                    case "13" -> validateAccountPin();
                    case "14" -> changeAccountPin();
                    case "0" -> {
                        running = false;
                        System.out.println("Thank you for using IgirePay!");
                    }
                    default -> System.out.println("Invalid option.");
                }
            } catch (SQLException exception) {
                System.out.println("Database error: "
                        + exception.getMessage());
            } catch (IllegalArgumentException exception) {
                System.out.println("Operation rejected: "
                        + exception.getMessage());
            } catch (Exception exception) {
                System.out.println("Unexpected error: "
                        + exception.getMessage());
            }
        }

        scanner.close();
    }

    private static void registerCustomer() throws SQLException {
        System.out.print("Full name: ");
        String name = scanner.nextLine().trim();

        System.out.print("Email: ");
        String email = scanner.nextLine().trim();

        System.out.print("Phone number: ");
        String phone = scanner.nextLine().trim();

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            System.out.println("All fields are required.");
            return;
        }

        Customer customer = new Customer(0, name, email, phone);
        int id = customerDAO.addCustomer(customer);
        System.out.println("Customer registered! ID: " + id);
    }

    private static void updateCustomer() throws SQLException {
        int id = readInt("Customer ID: ");
        Customer customer = customerDAO.getCustomerById(id);

        if (customer == null) {
            System.out.println("Customer not found.");
            return;
        }

        System.out.print("New full name: ");
        String name = scanner.nextLine().trim();

        System.out.print("New email: ");
        String email = scanner.nextLine().trim();

        System.out.print("New phone number: ");
        String phone = scanner.nextLine().trim();

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            System.out.println("All fields are required.");
            return;
        }

        customer.setFullName(name);
        customer.setEmail(email);
        customer.setPhoneNumber(phone);

        if (customerDAO.updateCustomer(customer)) {
            System.out.println("Customer updated successfully.");
        } else {
            System.out.println("Customer not updated.");
        }
    }

    private static void viewCustomers() throws SQLException {
        List<Customer> customers = customerDAO.getAllCustomers();

        if (customers.isEmpty()) {
            System.out.println("No customers found.");
            return;
        }

        for (Customer customer : customers) {
            System.out.println(customer);
        }
    }

    private static void viewCustomerAccounts() throws SQLException {
        int customerId = readInt("Customer ID: ");

        if (customerDAO.getCustomerById(customerId) == null) {
            System.out.println("Customer not found.");
            return;
        }

        List<Account> accounts =
                accountDAO.getAccountsByCustomerId(customerId);

        if (accounts.isEmpty()) {
            System.out.println("No active accounts found.");
            return;
        }

        for (Account account : accounts) {
            System.out.println(account);
        }
    }

    private static void viewAccountBalance() throws SQLException {
        int accountId = readInt("Account ID: ");
        Account account = accountDAO.getAccountById(accountId);

        if (account == null) {
            System.out.println("Account not found or inactive.");
            return;
        }

        System.out.println("Balance: " + account.getBalance() + " RWF");
    }

    private static void createAccount(String type) throws SQLException {
        int customerId = readInt("Customer ID: ");

        if (customerDAO.getCustomerById(customerId) == null) {
            System.out.println("Customer not found.");
            return;
        }

        BigDecimal balance = readAmount("Initial deposit (RWF): ");

        if (balance.signum() <= 0) {
            System.out.println("Initial deposit must be greater than zero.");
            return;
        }

        if (balance.scale() > 2) {
            System.out.println("Use at most 2 decimal places.");
            return;
        }

        if ("SAVINGS".equals(type)
                && balance.compareTo(new BigDecimal("5000")) < 0) {
            System.out.println(
                    "Savings account requires at least 5000 RWF.");
            return;
        }

        System.out.print("Create a 4-digit PIN: ");
        String pin = scanner.nextLine().trim();
        PinUtil.validatePin(pin);

        String hashedPin = PinUtil.hashPin(pin);
        Account account;

        if ("WALLET".equals(type)) {
            account = new WalletAccount(0, customerId, balance, hashedPin);
        } else {
            account = new SavingsAccount(0, customerId, balance, hashedPin);
        }

        int id = accountDAO.addAccount(account, type);
        System.out.println(type + " account created! Account ID: " + id);
    }

    private static void makePayment(String type) throws SQLException {
        int accountId = readInt("Account ID: ");

        if (accountDAO.getAccountById(accountId) == null) {
            System.out.println("Account not found or inactive.");
            return;
        }

        if (!authenticateAccount(accountId)) {
            System.out.println("Incorrect PIN. Transaction cancelled.");
            return;
        }

        BigDecimal amount = readAmount("Amount (RWF): ");

        if (amount.signum() <= 0) {
            System.out.println("Amount must be greater than zero.");
            return;
        }

        if (amount.scale() > 2) {
            System.out.println("Use at most 2 decimal places.");
            return;
        }

        System.out.print("Transaction reference ID: ");
        String reference = scanner.nextLine().trim();

        paymentService.processTransaction(
                accountId, reference, type, amount);
    }

    private static void transferMoney() throws SQLException {
        System.out.println("\n===== TRANSFER MONEY =====");

        int senderId = readInt("Sender account ID: ");
        int receiverId = readInt("Receiver account ID: ");

        if (senderId == receiverId) {
            System.out.println("Cannot transfer to the same account.");
            return;
        }

        Account sender = accountDAO.getAccountById(senderId);
        Account receiver = accountDAO.getAccountById(receiverId);

        if (sender == null || receiver == null) {
            System.out.println("Sender or receiver not found or inactive.");
            return;
        }

        if (!authenticateAccount(senderId)) {
            System.out.println("Incorrect PIN. Transfer cancelled.");
            return;
        }

        System.out.println("Sender balance: " + sender.getBalance() + " RWF");

        BigDecimal amount = readAmount("Amount to transfer (RWF): ");

        if (amount.signum() <= 0) {
            System.out.println("Amount must be greater than zero.");
            return;
        }

        if (amount.scale() > 2) {
            System.out.println("Use at most 2 decimal places.");
            return;
        }

        System.out.print("Transaction reference ID: ");
        String reference = scanner.nextLine().trim();

        paymentService.transferMoney(
                senderId, receiverId, reference, amount);
    }

    private static boolean authenticateAccount(int accountId)
            throws SQLException {
        System.out.print("Enter account PIN: ");
        String pin = scanner.nextLine().trim();
        return pinService.validatePin(accountId, pin);
    }

    private static void validateAccountPin() throws SQLException {
        int accountId = readInt("Account ID: ");

        if (accountDAO.getAccountById(accountId) == null) {
            System.out.println("Account not found or inactive.");
            return;
        }

        if (authenticateAccount(accountId)) {
            System.out.println("PIN validated successfully.");
        } else {
            System.out.println("Incorrect PIN.");
        }
    }

    private static void changeAccountPin() throws SQLException {
        int accountId = readInt("Account ID: ");

        if (accountDAO.getAccountById(accountId) == null) {
            System.out.println("Account not found or inactive.");
            return;
        }

        System.out.print("Current PIN: ");
        String oldPin = scanner.nextLine().trim();

        System.out.print("New 4-digit PIN: ");
        String newPin = scanner.nextLine().trim();

        System.out.print("Confirm new PIN: ");
        String confirmation = scanner.nextLine().trim();

        if (!newPin.equals(confirmation)) {
            System.out.println("PIN confirmation does not match.");
            return;
        }

        pinService.changePin(accountId, oldPin, newPin);
        System.out.println("PIN changed successfully.");
    }

    private static void viewTransactionHistory() throws SQLException {
        int accountId = readInt("Account ID: ");

        if (accountDAO.getAccountById(accountId) == null) {
            System.out.println("Account not found or inactive.");
            return;
        }

        List<Transaction> transactions =
                transactionDAO.getTransactionsByAccountId(accountId);

        if (transactions.isEmpty()) {
            System.out.println("No transactions found.");
            return;
        }

        System.out.println("\n===== TRANSACTION HISTORY =====");
        for (Transaction transaction : transactions) {
            System.out.println(transaction);
        }
    }

    private static void deactivateAccount() throws SQLException {
        int accountId = readInt("Account ID to deactivate: ");

        if (!accountDAO.isAccountActive(accountId)) {
            System.out.println("Account not found or already inactive.");
            return;
        }

        accountDAO.deactivateAccount(accountId);
        System.out.println("Account deactivated successfully. "
                + "Transaction history has been preserved.");
    }

    private static BigDecimal readAmount(String message) {
        System.out.print(message);
        String input = scanner.nextLine().trim();

        try {
            BigDecimal amount = new BigDecimal(input);

            if (amount.signum() < 0) {
                throw new IllegalArgumentException(
                        "Amount cannot be negative.");
            }

            return amount;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "Please enter a valid amount.");
        }
    }

    private static int readInt(String message) {
        System.out.print(message);
        String input = scanner.nextLine().trim();

        try {
            int number = Integer.parseInt(input);

            if (number <= 0) {
                throw new IllegalArgumentException(
                        "ID must be positive.");
            }

            return number;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "Please enter a valid number.");
        }
    }
}
