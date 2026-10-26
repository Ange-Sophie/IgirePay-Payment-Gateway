
package com.igirepay.model;

import java.util.ArrayList;
import java.util.List;

public class Customer {

    private int id;
    private String fullName;
    private String email;
    private String phoneNumber;
    private List<Account> accounts;

    public Customer(int id, String fullName,
                    String email, String phoneNumber) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.accounts = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public List<Account> getAccounts() {
        return List.copyOf(accounts);
    }

    public void addAccount(Account account) {
        if (account == null || account.getCustomerId() != id) {
            throw new IllegalArgumentException(
                    "Account does not belong to this customer."
            );
        }
        accounts.add(account);
    }

    @Override
    public String toString() {
        return "Customer{" +
                "id=" + id +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                '}';
    }
}
