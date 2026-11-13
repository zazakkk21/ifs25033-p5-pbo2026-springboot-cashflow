package org.delcom.ifs25033springbootcashflow.adapter.repository;

import org.delcom.ifs25033springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25033springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25033springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25033springbootcashflow.domain.entity.TransactionSummary;
import org.delcom.ifs25033springbootcashflow.domain.repository.ITransactionRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class JdbcTransactionRepository implements ITransactionRepository {
    private static final RowMapper<Transaction> TRANSACTION_ROW_MAPPER = (row, index) -> new Transaction(
            row.getInt("id"),
            row.getInt("user_id"),
            row.getString("description"),
            row.getLong("amount"),
            TransactionType.valueOf(row.getString("type")),
            row.getString("proof_path"),
            row.getTimestamp("created_at").toLocalDateTime());
    private final JdbcTemplate jdbc;

    public JdbcTransactionRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override public List<Transaction> findAll() {
        return jdbc.query("SELECT * FROM transactions ORDER BY created_at DESC, id DESC", TRANSACTION_ROW_MAPPER);
    }

    @Override public Optional<Transaction> findById(Integer id) {
        return jdbc.query("SELECT * FROM transactions WHERE id = ?", TRANSACTION_ROW_MAPPER, id)
                .stream().findFirst();
    }

    @Override public List<Transaction> findByUserId(int userId) {
        return jdbc.query("SELECT * FROM transactions WHERE user_id = ? ORDER BY created_at DESC, id DESC",
                TRANSACTION_ROW_MAPPER, userId);
    }

    @Override public List<Transaction> searchByUserId(int userId, String keyword, SortOption sort,
                                                       int offset, int limit) {
        String orderBy = switch (sort) {
            case NEWEST -> "created_at DESC, id DESC";
            case OLDEST -> "created_at ASC, id ASC";
            case AMOUNT_ASC -> "amount ASC, id DESC";
            case AMOUNT_DESC -> "amount DESC, id DESC";
            case INCOME_FIRST -> "CASE WHEN type = 'INCOME' THEN 0 ELSE 1 END, created_at DESC, id DESC";
            case EXPENSE_FIRST -> "CASE WHEN type = 'EXPENSE' THEN 0 ELSE 1 END, created_at DESC, id DESC";
        };
        String query = normalizeKeyword(keyword);
        String sql = "SELECT * FROM transactions WHERE user_id = ? AND LOWER(description) LIKE ? ESCAPE '!' "
                + "ORDER BY " + orderBy + " LIMIT ? OFFSET ?";
        return jdbc.query(sql, TRANSACTION_ROW_MAPPER, userId, query, limit, offset);
    }

    @Override public int countByUserId(int userId, String keyword) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM transactions WHERE user_id = ? AND LOWER(description) LIKE ? ESCAPE '!'",
                Integer.class, userId, normalizeKeyword(keyword));
        return count == null ? 0 : count;
    }

    @Override public TransactionSummary getBalance(int userId) {
        return jdbc.queryForObject("""
                SELECT COALESCE(SUM(CASE WHEN type = 'INCOME' THEN amount ELSE 0 END), 0) AS total_income,
                       COALESCE(SUM(CASE WHEN type = 'EXPENSE' THEN amount ELSE 0 END), 0) AS total_expense,
                       COUNT(*) AS transaction_count
                FROM transactions
                WHERE user_id = ?
                """, (row, index) -> {
            long income = row.getLong("total_income");
            long expense = row.getLong("total_expense");
            return new TransactionSummary(income, expense, income - expense, row.getInt("transaction_count"));
        }, userId);
    }

    @Override public Transaction save(int userId, String description, long amount,
                                      TransactionType type, String proofPath) {
        LocalDateTime createdAt = LocalDateTime.now();
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO transactions (user_id, description, amount, type, proof_path, created_at) "
                            + "VALUES (?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            statement.setInt(1, userId);
            statement.setString(2, description);
            statement.setLong(3, amount);
            statement.setString(4, type.name());
            statement.setString(5, proofPath);
            statement.setTimestamp(6, Timestamp.valueOf(createdAt));
            return statement;
        }, keyHolder);
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("Database tidak mengembalikan ID transaksi");
        }
        return new Transaction(key.intValue(), userId, description, amount, type, proofPath, createdAt);
    }

    @Override public Transaction save(Transaction transaction) {
        jdbc.update("INSERT INTO transactions "
                        + "(id, user_id, description, amount, type, proof_path, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)",
                transaction.getId(), transaction.getUserId(), transaction.getDescription(), transaction.getAmount(),
                transaction.getType().name(), transaction.getProofPath(), Timestamp.valueOf(transaction.getCreatedAt()));
        return transaction;
    }

    @Override public boolean deleteById(Integer id) {
        return jdbc.update("DELETE FROM transactions WHERE id = ?", id) > 0;
    }

    @Override public void update(Transaction transaction) {
        jdbc.update("UPDATE transactions SET description = ?, amount = ?, type = ?, proof_path = ? WHERE id = ?",
                transaction.getDescription(), transaction.getAmount(), transaction.getType().name(),
                transaction.getProofPath(), transaction.getId());
    }

    @Override public List<Transaction> findBy(java.util.function.Predicate<Transaction> predicate) {
        return findAll().stream().filter(predicate).toList();
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return "%";
        }
        String escaped = keyword.trim().toLowerCase(java.util.Locale.ROOT)
                .replace("!", "!!")
                .replace("%", "!%")
                .replace("_", "!_");
        return "%" + escaped + "%";
    }
}
