package org.delcom.ifs25033springbootcashflow.domain.entity;

import java.util.Comparator;

public enum SortOption {
    NEWEST(Comparator.comparing(Transaction::getCreatedAt).reversed()),
    OLDEST(Comparator.comparing(Transaction::getCreatedAt)),
    AMOUNT_ASC(Comparator.comparingLong(Transaction::getAmount)),
    AMOUNT_DESC(Comparator.comparingLong(Transaction::getAmount).reversed()),
    INCOME_FIRST(Comparator.comparing(transaction -> transaction.getType() != TransactionType.INCOME)),
    EXPENSE_FIRST(Comparator.comparing(transaction -> transaction.getType() != TransactionType.EXPENSE));

    private final Comparator<Transaction> comparator;

    SortOption(Comparator<Transaction> comparator) {
        this.comparator = comparator;
    }

    public Comparator<Transaction> comparator() {
        return comparator;
    }
}
