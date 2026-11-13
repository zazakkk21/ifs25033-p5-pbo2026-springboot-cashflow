package org.delcom.ifs25033springbootcashflow.adapter.presenter;

import org.delcom.ifs25033springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25033springbootcashflow.domain.entity.TransactionSummary;
import org.delcom.ifs25033springbootcashflow.domain.entity.TransactionType;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class FinancePresenter {
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", Locale.forLanguageTag("id-ID"));

    public String formatMoney(long amount) {
        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.US);
        formatter.setMinimumFractionDigits(2);
        formatter.setMaximumFractionDigits(2);
        return "Rp " + formatter.format(amount);
    }

    public String formatTransaction(Transaction transaction) {
        String type = transaction.getType() == TransactionType.INCOME ? "Pemasukan" : "Pengeluaran";
        return "[" + transaction.getId() + "] " + transaction.getDescription() + " | "
                + formatMoney(transaction.getAmount()) + " | " + type;
    }

    public String formatBalance(TransactionSummary balance) {
        return "Saldo: " + formatMoney(balance.balance());
    }

    public String formatDate(Transaction transaction) {
        return transaction.getCreatedAt().format(DATE_FORMAT);
    }
}
