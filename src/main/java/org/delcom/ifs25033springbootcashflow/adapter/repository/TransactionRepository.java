package org.delcom.ifs25033springbootcashflow.adapter.repository;

import org.delcom.ifs25033springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25033springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25033springbootcashflow.domain.repository.ITransactionRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TransactionRepository extends InMemoryRepository<Transaction, Integer>
        implements ITransactionRepository {
    private final AtomicInteger nextId = new AtomicInteger(1);

    public TransactionRepository() {
        super(Transaction::getId);
    }

    @Override public List<Transaction> findByUserId(int userId) {
        return findBy(transaction -> transaction.getUserId() == userId);
    }

    @Override public Transaction save(int userId, String description, long amount,
                                      TransactionType type, String proofPath) {
        Transaction transaction = new Transaction(nextId.getAndIncrement(), userId, description,
                amount, type, proofPath, LocalDateTime.now());
        return save(transaction);
    }
}
