
package com.igirepay.service;

import com.igirepay.model.Transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TransactionManager {

    private final List<Transaction> transactions =
            new ArrayList<>();

    private final Set<String> processedReferences =
            new HashSet<>();

    private final Map<String, String> failedTransactions =
            new HashMap<>();

    public boolean isDuplicate(String referenceId) {
        return processedReferences.contains(referenceId);
    }

    public boolean recordTransaction(Transaction transaction) {

        if (transaction == null ||
                transaction.getReferenceId() == null ||
                transaction.getReferenceId().isBlank()) {
            throw new IllegalArgumentException(
                    "A valid transaction reference is required."
            );
        }

        String referenceId = transaction.getReferenceId();

        if (!processedReferences.add(referenceId)) {
            return false;
        }

        transactions.add(transaction);
        return true;
    }

    public void logFailure(String referenceId, String reason) {
        failedTransactions.put(referenceId, reason);
    }

    public List<Transaction> getTransactions() {
        return List.copyOf(transactions);
    }

    public Map<String, String> getFailedTransactions() {
        return Map.copyOf(failedTransactions);
    }
}
