package org.delcom.ifs25033springbootcashflow.usecase;

import org.delcom.ifs25033springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25033springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25033springbootcashflow.domain.entity.TransactionSummary;
import org.delcom.ifs25033springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25033springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25033springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25033springbootcashflow.domain.repository.ITransactionRepository;

import java.util.List;
import java.util.Locale;

public class FinanceUseCase {
    private final ITransactionRepository transactionRepository;

    public FinanceUseCase(ITransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public List<Transaction> getTransactions(int userId) {
        return transactionRepository.findByUserId(userId);
    }

    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    public List<Transaction> searchTransactions(int userId, String keyword) {
        String query = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        return getTransactions(userId).stream()
                .filter(transaction -> transaction.getDescription().toLowerCase(Locale.ROOT).contains(query))
                .toList();
    }

    public List<Transaction> searchTransactions(int userId, String keyword, SortOption option,
                                                int offset, int limit) {
        if (option == null || offset < 0 || limit < 1) {
            throw new IllegalArgumentException("Halaman atau urutan transaksi tidak valid");
        }
        return transactionRepository.searchByUserId(userId, keyword, option, offset, limit);
    }

    public int countTransactions(int userId, String keyword) {
        return transactionRepository.countByUserId(userId, keyword);
    }

    public List<Transaction> sortTransactions(int userId, SortOption option) throws ValidationException {
        if (option == null) {
            throw new ValidationException("Urutan transaksi tidak valid");
        }
        return getTransactions(userId).stream().sorted(option.comparator()).toList();
    }

    public Transaction addTransaction(int userId, String description, long amount,
                                      TransactionType type, String proofPath) throws ValidationException {
        validate(description, amount, type);
        return transactionRepository.save(userId, description.trim(), amount, type, proofPath);
    }

    public Transaction updateTransaction(int userId, int id, String description, long amount,
                                         TransactionType type, String proofPath)
            throws EntityNotFoundException, ValidationException {
        Transaction transaction = requireOwnedTransaction(userId, id);
        validate(description, amount, type);
        transaction.update(description.trim(), amount, type, proofPath);
        transactionRepository.update(transaction);
        return transaction;
    }

    public void removeTransaction(int userId, int id) throws EntityNotFoundException, ValidationException {
        requireOwnedTransaction(userId, id);
        if (!transactionRepository.deleteById(id)) {
            throw new EntityNotFoundException(id);
        }
    }

    public Transaction requireOwnedTransaction(int userId, int id)
            throws EntityNotFoundException, ValidationException {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(id));
        if (transaction.getUserId() != userId) {
            throw new ValidationException("Transaksi tidak dimiliki pengguna ini");
        }
        return transaction;
    }

    public TransactionSummary getBalance(int userId) {
        return transactionRepository.getBalance(userId);
    }

    private void validate(String description, long amount, TransactionType type) throws ValidationException {
        if (description == null || description.isBlank() || description.trim().length() > 2000) {
            throw new ValidationException("Keterangan wajib diisi (maksimal 2000 karakter)");
        }
        if (amount <= 0) {
            throw new ValidationException("Jumlah harus lebih besar dari nol");
        }
        if (type == null) {
            throw new ValidationException("Tipe transaksi tidak valid");
        }
    }
}
