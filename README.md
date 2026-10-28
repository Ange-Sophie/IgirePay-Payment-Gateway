# IgirePay Payment Gateway

## Project Overview

IgirePay is a Java console application that demonstrates digital wallet operations using MySQL/MariaDB and JDBC.

## Main Features

- Register and update customers
- Create wallet and savings accounts
- View accounts and balances
- Deposit, withdraw, and transfer money
- View transaction history
- Deactivate accounts
- Validate and change account PINs
- Export transaction reports and customer statements to CSV

## Technology Stack

- Java
- Maven
- JDBC
- MySQL/MariaDB through XAMPP
- Git and GitHub

## Package Structure

### com.igirepay
Contains Main, which displays the console menu, accepts user input, and calls the appropriate application functionality.

### com.igirepay.model
Contains model classes that represent application data, such as customers, accounts, and transactions. These classes support object-oriented programming.

### com.igirepay.dao
Contains Data Access Objects. DAO classes handle SQL queries and database operations, separating data persistence from the user interface.

### com.igirepay.service
Contains business logic and coordinates operations such as payments, PIN validation, and report generation.

### com.igirepay.util
Contains shared utilities. DBConnection provides database connections, while PinUtil handles PIN validation and hashing.

### com.igirepay.report
Contains reporting-related classes that export transaction information and customer statements.

## Database

The application uses the igirepay_db MySQL/MariaDB database. The project includes customer, account, transaction, and processed-request data.

## Running the Application

1. Start MySQL in XAMPP.
2. Ensure the igirepay_db database and required tables exist.
3. Check the database connection configuration in DBConnection.
4. Open the project in IntelliJ IDEA.
5. Allow Maven dependencies to load.
6. Run the Main class.

## Security Note

Use test data for demonstrations. Do not commit real PINs, passwords, or database credentials to GitHub.
