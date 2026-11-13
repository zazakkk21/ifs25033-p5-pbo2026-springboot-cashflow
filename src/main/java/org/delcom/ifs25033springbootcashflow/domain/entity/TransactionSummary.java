package org.delcom.ifs25033springbootcashflow.domain.entity;

public record TransactionSummary(long totalIncome, long totalExpense, long balance, int transactionCount) {
}
