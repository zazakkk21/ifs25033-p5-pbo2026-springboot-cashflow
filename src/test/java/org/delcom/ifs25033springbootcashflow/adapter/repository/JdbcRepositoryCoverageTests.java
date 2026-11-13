package org.delcom.ifs25033springbootcashflow.adapter.repository;

import org.delcom.ifs25033springbootcashflow.domain.entity.Role;
import org.delcom.ifs25033springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25033springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25033springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25033springbootcashflow.domain.entity.User;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.datasource.AbstractDataSource;
import org.springframework.jdbc.support.KeyHolder;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JdbcRepositoryCoverageTests {
    private JdbcTemplate jdbc;
    private JdbcUserRepository users;
    private JdbcTransactionRepository transactions;

    @BeforeEach
    void setUp() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:" + UUID.randomUUID().toString().replace("-", "")
                + ";DB_CLOSE_DELAY=-1");
        dataSource.setUser("sa");
        SchemaInitializer.initialize(dataSource);
        jdbc = new JdbcTemplate(dataSource);
        users = new JdbcUserRepository(jdbc);
        transactions = new JdbcTransactionRepository(jdbc);
    }

    @Test
    void jdbcUserRepositorySupportsGeneratedAndExplicitIdsAndCrud() {
        User alice = users.saveNew("alice", "hash", "Alice", Role.USER);
        assertTrue(alice.getId() > 0);
        assertEquals(1, users.findAll().size());
        assertEquals(alice.getId(), users.findById(alice.getId()).orElseThrow().getId());
        assertTrue(users.findById(-1).isEmpty());
        assertEquals(alice.getId(), users.findByUsername("ALICE").orElseThrow().getId());
        assertTrue(users.findByUsername("missing").isEmpty());
        assertEquals(1, users.findBy(user -> user.getRole() == Role.USER).size());
        assertTrue(users.findBy(user -> user.getRole() == Role.ADMIN).isEmpty());

        User admin = new User(500, "root", "secret-hash", "Root", Role.ADMIN, null);
        assertEquals(admin, users.save(admin));
        admin.changePasswordHash("updated-hash");
        admin.changeFullName("Updated Root");
        admin.changePhotoPath("root.png");
        users.update(admin);
        User updated = users.findById(500).orElseThrow();
        assertEquals("updated-hash", updated.getPasswordHash());
        assertEquals("Updated Root", updated.getFullName());
        assertEquals("root.png", updated.getPhotoPath());
        assertTrue(users.deleteById(500));
        assertFalse(users.deleteById(500));
    }

    @Test
    void jdbcTransactionRepositoryFiltersSortsCountsAndSummarizes() {
        User user = users.saveNew("alice", "hash", "Alice", Role.USER);
        User otherUser = users.saveNew("bob", "hash", "Bob", Role.USER);
        int userId = user.getId();
        LocalDateTime base = LocalDateTime.of(2026, 2, 1, 10, 0);
        transactions.save(transaction(10, userId, "salary", 300, TransactionType.INCOME, base));
        transactions.save(transaction(11, userId, "food", 100, TransactionType.EXPENSE, base.plusDays(2)));
        transactions.save(transaction(12, userId, "pay%_!roll", 200, TransactionType.INCOME, base.plusDays(1)));
        transactions.save(transaction(13, otherUser.getId(), "other", 999,
                TransactionType.INCOME, base.plusDays(3)));

        assertEquals(List.of(13, 11, 12, 10), ids(transactions.findAll()));
        assertEquals(List.of(11, 12, 10), ids(transactions.findByUserId(userId)));
        assertTrue(transactions.findById(999).isEmpty());
        assertEquals(List.of(11, 12, 10), ids(transactions.findBy(transaction ->
                transaction.getUserId() == userId)));

        assertEquals(List.of(11, 12, 10), ids(transactions.searchByUserId(
                userId, null, SortOption.NEWEST, 0, 10)));
        assertEquals(List.of(10, 12, 11), ids(transactions.searchByUserId(
                userId, " ", SortOption.OLDEST, 0, 10)));
        assertEquals(List.of(11, 12, 10), ids(transactions.searchByUserId(
                userId, null, SortOption.AMOUNT_ASC, 0, 10)));
        assertEquals(List.of(10, 12, 11), ids(transactions.searchByUserId(
                userId, null, SortOption.AMOUNT_DESC, 0, 10)));
        assertEquals(List.of(12, 10, 11), ids(transactions.searchByUserId(
                userId, null, SortOption.INCOME_FIRST, 0, 10)));
        assertEquals(List.of(11, 12, 10), ids(transactions.searchByUserId(
                userId, null, SortOption.EXPENSE_FIRST, 0, 10)));
        assertEquals(List.of(12), ids(transactions.searchByUserId(
                userId, "PAY", SortOption.NEWEST, 0, 10)));
        assertEquals(List.of(12), ids(transactions.searchByUserId(
                userId, "%", SortOption.NEWEST, 0, 10)));
        assertEquals(List.of(12), ids(transactions.searchByUserId(
                userId, "_", SortOption.NEWEST, 0, 10)));
        assertEquals(List.of(12), ids(transactions.searchByUserId(
                userId, "!", SortOption.NEWEST, 0, 10)));
        assertEquals(List.of(12), ids(transactions.searchByUserId(
                userId, null, SortOption.NEWEST, 1, 1)));

        assertEquals(3, transactions.countByUserId(userId, null));
        assertEquals(3, transactions.countByUserId(userId, " "));
        assertEquals(1, transactions.countByUserId(userId, "pay"));
        assertEquals(1, transactions.countByUserId(userId, "%"));
        assertEquals(0, transactions.countByUserId(userId + 99, null));
        assertEquals(500, transactions.getBalance(userId).totalIncome());
        assertEquals(100, transactions.getBalance(userId).totalExpense());
        assertEquals(400, transactions.getBalance(userId).balance());
        assertEquals(3, transactions.getBalance(userId).transactionCount());
        assertEquals(0, transactions.getBalance(999).balance());

        Transaction changed = transaction(10, userId, "updated", 350, TransactionType.EXPENSE, base);
        transactions.update(changed);
        Transaction reloaded = transactions.findById(10).orElseThrow();
        assertEquals("updated", reloaded.getDescription());
        assertEquals(350, reloaded.getAmount());
        assertEquals(TransactionType.EXPENSE, reloaded.getType());
        assertTrue(transactions.deleteById(10));
        assertFalse(transactions.deleteById(10));
    }

    @Test
    void jdbcTransactionSaveGeneratesAnIdAndStoresNullableProof() {
        User user = users.saveNew("alice", "hash", "Alice", Role.USER);

        Transaction created = transactions.save(user.getId(), "Generated", 45,
                TransactionType.INCOME, null);

        assertNotNull(created.getId());
        assertEquals(user.getId(), transactions.findById(created.getId()).orElseThrow().getUserId());
        assertEquals(null, created.getProofPath());
    }

    @Test
    void schemaInitializerReportsConnectionFailures() {
        DataSource unavailable = new AbstractDataSource() {
            @Override
            public Connection getConnection() throws SQLException {
                throw new SQLException("database unavailable");
            }

            @Override
            public Connection getConnection(String username, String password) throws SQLException {
                throw new SQLException("database unavailable");
            }
        };

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> SchemaInitializer.initialize(unavailable));
        assertEquals("Gagal menyiapkan tabel cashflow", exception.getMessage());
        assertEquals("database unavailable", exception.getCause().getMessage());
    }

    @Test
    void jdbcRepositoriesReportMissingGeneratedKeysAndNullCountResults() {
        JdbcTemplate jdbcWithoutGeneratedKeys = mock(JdbcTemplate.class);
        doReturn(1).when(jdbcWithoutGeneratedKeys)
                .update(any(PreparedStatementCreator.class), any(KeyHolder.class));
        JdbcUserRepository userRepository = new JdbcUserRepository(jdbcWithoutGeneratedKeys);
        JdbcTransactionRepository transactionRepository = new JdbcTransactionRepository(jdbcWithoutGeneratedKeys);

        assertThrows(IllegalStateException.class,
                () -> userRepository.saveNew("user", "hash", "User", Role.USER));
        assertThrows(IllegalStateException.class,
                () -> transactionRepository.save(1, "Description", 10, TransactionType.INCOME, null));

        JdbcTemplate jdbcWithoutCount = mock(JdbcTemplate.class);
        when(jdbcWithoutCount.queryForObject(anyString(), eq(Integer.class), any())).thenReturn(null);
        assertEquals(0, new JdbcTransactionRepository(jdbcWithoutCount).countByUserId(1, null));
    }

    private static Transaction transaction(int id, int userId, String description, long amount,
                                           TransactionType type, LocalDateTime createdAt) {
        return new Transaction(id, userId, description, amount, type, null, createdAt);
    }

    private static List<Integer> ids(List<Transaction> transactions) {
        return transactions.stream().map(Transaction::getId).toList();
    }
}
