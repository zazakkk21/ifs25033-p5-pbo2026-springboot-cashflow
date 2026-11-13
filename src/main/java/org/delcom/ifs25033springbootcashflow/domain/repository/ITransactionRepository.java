package org.delcom.ifs25033springbootcashflow.domain.repository;

import org.delcom.ifs25033springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25033springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25033springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25033springbootcashflow.domain.entity.TransactionSummary;

import java.util.List;

public interface ITransactionRepository extends IRepository<Transaction, Integer> {
    List<Transaction> findByUserId(int userId);
    Transaction save(int userId, String description, long amount, TransactionType type, String proofPath);

    default List<Transaction> searchByUserId(int userId, String keyword, SortOption sort, int offset, int limit) {
        return findByUserId(userId).stream()
                .filter(transaction -> keyword == null || keyword.isBlank()
                        || transaction.getDescription().toLowerCase(java.util.Locale.ROOT)
                        .contains(keyword.toLowerCase(java.util.Locale.ROOT)))
                .sorted(sort.comparator())
                .skip(offset)
                .limit(limit)
                .toList();
    }

    default int countByUserId(int userId, String keyword) {
        return (int) findByUserId(userId).stream()
                .filter(transaction -> keyword == null || keyword.isBlank()
                        || transaction.getDescription().toLowerCase(java.util.Locale.ROOT)
                        .contains(keyword.toLowerCase(java.util.Locale.ROOT)))
                .count();
    }

    default TransactionSummary getBalance(int userId) {
        List<Transaction> transactions = findByUserId(userId);
        long income = transactions.stream()
                .filter(transaction -> transaction.getType() == TransactionType.INCOME)
                .mapToLong(Transaction::getAmount).sum();
        long expense = transactions.stream()
                .filter(transaction -> transaction.getType() == TransactionType.EXPENSE)
                .mapToLong(Transaction::getAmount).sum();
        return new TransactionSummary(income, expense, income - expense, transactions.size());
    }
}
