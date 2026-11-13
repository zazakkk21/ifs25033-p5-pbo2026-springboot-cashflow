package org.delcom.ifs25033springbootcashflow;

import org.delcom.ifs25033springbootcashflow.adapter.repository.TransactionRepository;
import org.delcom.ifs25033springbootcashflow.adapter.repository.UserRepository;
import org.delcom.ifs25033springbootcashflow.domain.entity.Role;
import org.delcom.ifs25033springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25033springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25033springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25033springbootcashflow.domain.entity.User;
import org.delcom.ifs25033springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25033springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25033springbootcashflow.usecase.AuthUseCase;
import org.delcom.ifs25033springbootcashflow.usecase.FinanceUseCase;
import org.delcom.ifs25033springbootcashflow.usecase.UserUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UseCaseCoverageTests {
    private static final LocalDateTime FIRST_DATE = LocalDateTime.of(2026, 1, 1, 10, 0);

    @Test
    void authenticationValidatesCredentialsAndSupportsRegistrationAndLogin() throws Exception {
        UserRepository repository = new UserRepository();
        AuthUseCase auth = new AuthUseCase(repository);

        assertValidation(() -> auth.register(null, "pass", "Name"));
        assertValidation(() -> auth.register(" ", "pass", "Name"));
        assertValidation(() -> auth.register("u".repeat(101), "pass", "Name"));
        assertValidation(() -> auth.register("user", null, "Name"));
        assertValidation(() -> auth.register("user", "123", "Name"));
        assertValidation(() -> auth.register("user", "pass", null));
        assertValidation(() -> auth.register("user", "pass", " "));
        assertValidation(() -> auth.register("user", "pass", "n".repeat(201)));

        User registered = auth.register(" alice ", "secret", " Alice Example ");
        assertEquals("alice", registered.getUsername());
        assertEquals("Alice Example", registered.getFullName());
        assertEquals(Role.USER, registered.getRole());
        assertTrue(auth.login("ALICE", "secret").getId() == registered.getId());
        assertValidation(() -> auth.register("alice", "other", "Another"));

        assertValidation(() -> auth.login(null, "secret"));
        assertValidation(() -> auth.login(" ", "secret"));
        assertValidation(() -> auth.login("alice", null));
        assertValidation(() -> auth.login("alice", ""));
        assertValidation(() -> auth.login("unknown", "secret"));
        assertValidation(() -> auth.login("alice", "incorrect"));
    }

    @Test
    void rootAdminValidationCreatesAdminOnceAndReusesExistingUser() throws Exception {
        UserRepository repository = new UserRepository();
        AuthUseCase auth = new AuthUseCase(repository);

        assertValidation(() -> auth.ensureRootAdmin(null, "secret"));
        assertValidation(() -> auth.ensureRootAdmin(" ", "secret"));
        assertValidation(() -> auth.ensureRootAdmin("root", null));
        assertValidation(() -> auth.ensureRootAdmin("root", "123"));

        User admin = auth.ensureRootAdmin(" root ", "secret");
        assertEquals(Role.ADMIN, admin.getRole());
        assertEquals(admin.getId(), auth.ensureRootAdmin("root", "other").getId());
        assertEquals(1, repository.findAll().size());
    }

    @Test
    void financeSearchSortAndSummaryCoverAllSortOptions() throws Exception {
        TransactionRepository repository = new TransactionRepository();
        FinanceUseCase finance = new FinanceUseCase(repository);
        repository.save(transaction(1, 1, "Alpha", 100, TransactionType.INCOME, FIRST_DATE));
        repository.save(transaction(2, 1, "Beta", 50, TransactionType.EXPENSE, FIRST_DATE.plusDays(1)));
        repository.save(transaction(3, 1, "Alphabet", 25, TransactionType.INCOME, FIRST_DATE.plusDays(2)));
        repository.save(transaction(4, 2, "Other user", 900, TransactionType.INCOME, FIRST_DATE.plusDays(3)));

        assertEquals(3, finance.getTransactions(1).size());
        assertEquals(4, finance.getAllTransactions().size());
        assertEquals(3, finance.searchTransactions(1, null).size());
        assertEquals(2, finance.searchTransactions(1, " ALPHA ").size());
        assertTrue(finance.searchTransactions(1, "missing").isEmpty());
        assertEquals(2, finance.searchTransactions(1, "alpha", SortOption.NEWEST, 0, 10).size());
        assertEquals(3, finance.searchTransactions(1, " ", SortOption.NEWEST, 0, 10).size());
        assertTrue(finance.searchTransactions(1, "missing", SortOption.NEWEST, 0, 10).isEmpty());

        assertEquals(List.of(3, 2, 1), ids(finance.sortTransactions(1, SortOption.NEWEST)));
        assertEquals(List.of(1, 2, 3), ids(finance.sortTransactions(1, SortOption.OLDEST)));
        assertEquals(List.of(3, 2, 1), ids(finance.sortTransactions(1, SortOption.AMOUNT_ASC)));
        assertEquals(List.of(1, 2, 3), ids(finance.sortTransactions(1, SortOption.AMOUNT_DESC)));
        assertEquals(List.of(1, 3, 2), ids(finance.sortTransactions(1, SortOption.INCOME_FIRST)));
        assertEquals(List.of(2, 1, 3), ids(finance.sortTransactions(1, SortOption.EXPENSE_FIRST)));
        assertThrows(ValidationException.class, () -> finance.sortTransactions(1, null));

        assertThrows(IllegalArgumentException.class,
                () -> finance.searchTransactions(1, null, null, 0, 10));
        assertThrows(IllegalArgumentException.class,
                () -> finance.searchTransactions(1, null, SortOption.NEWEST, -1, 10));
        assertThrows(IllegalArgumentException.class,
                () -> finance.searchTransactions(1, null, SortOption.NEWEST, 0, 0));
        assertEquals(List.of(3, 2), ids(finance.searchTransactions(1, null, SortOption.NEWEST, 0, 2)));
        assertEquals(3, finance.countTransactions(1, null));
        assertEquals(2, finance.countTransactions(1, "alpha"));
        assertEquals(3, repository.countByUserId(1, " "));
        assertEquals(3, repository.getBalance(1).transactionCount());
        assertEquals(125, repository.getBalance(1).totalIncome());
        assertEquals(50, repository.getBalance(1).totalExpense());
        assertEquals(0, repository.getBalance(99).balance());
    }

    @Test
    void financeValidatesMutatesAndDeletesOnlyOwnedTransactions() throws Exception {
        TransactionRepository repository = new TransactionRepository();
        FinanceUseCase finance = new FinanceUseCase(repository);

        assertValidation(() -> finance.addTransaction(1, null, 10, TransactionType.INCOME, null));
        assertValidation(() -> finance.addTransaction(1, " ", 10, TransactionType.INCOME, null));
        assertValidation(() -> finance.addTransaction(1, "x".repeat(2001), 10, TransactionType.INCOME, null));
        assertValidation(() -> finance.addTransaction(1, "Valid", 0, TransactionType.INCOME, null));
        assertValidation(() -> finance.addTransaction(1, "Valid", -1, TransactionType.INCOME, null));
        assertValidation(() -> finance.addTransaction(1, "Valid", 10, null, null));

        Transaction transaction = finance.addTransaction(1, "Original", 100, TransactionType.INCOME, null);
        assertEquals("Original", finance.requireOwnedTransaction(1, transaction.getId()).getDescription());
        assertThrows(EntityNotFoundException.class, () -> finance.requireOwnedTransaction(1, 999));
        assertValidation(() -> finance.requireOwnedTransaction(2, transaction.getId()));
        assertThrows(EntityNotFoundException.class,
                () -> finance.updateTransaction(1, 999, "Update", 50, TransactionType.EXPENSE, null));
        assertValidation(() -> finance.updateTransaction(2, transaction.getId(),
                "Update", 50, TransactionType.EXPENSE, null));
        assertValidation(() -> finance.updateTransaction(1, transaction.getId(),
                null, 50, TransactionType.EXPENSE, null));

        Transaction updated = finance.updateTransaction(1, transaction.getId(),
                " Updated ", 75, TransactionType.EXPENSE, "proof.png");
        assertEquals("Updated", updated.getDescription());
        assertEquals(75, updated.getAmount());
        assertEquals(TransactionType.EXPENSE, updated.getType());
        assertEquals("proof.png", updated.getProofPath());

        assertThrows(EntityNotFoundException.class, () -> finance.removeTransaction(1, 999));
        assertValidation(() -> finance.removeTransaction(2, transaction.getId()));
        finance.removeTransaction(1, transaction.getId());
        assertTrue(finance.getTransactions(1).isEmpty());

        TransactionRepository cannotDelete = new TransactionRepository() {
            @Override
            public boolean deleteById(Integer id) {
                return false;
            }
        };
        Transaction undeleted = cannotDelete.save(7, "Existing", 1, TransactionType.INCOME, null);
        assertThrows(EntityNotFoundException.class,
                () -> new FinanceUseCase(cannotDelete).removeTransaction(7, undeleted.getId()));
    }

    @Test
    void userRepositoryAndUseCaseCoverProfileValidationAndUpdates() throws Exception {
        UserRepository repository = new UserRepository();
        UserUseCase users = new UserUseCase(repository);
        User user = repository.saveNew("user", "old-hash", "Original", Role.USER);

        assertEquals(1, users.listUsers().size());
        assertEquals(user.getId(), users.getUser(user.getId()).getId());
        assertThrows(EntityNotFoundException.class, () -> users.getUser(999));

        assertValidation(() -> users.updateProfile(user.getId(), null, null));
        assertValidation(() -> users.updateProfile(user.getId(), " ", null));
        assertValidation(() -> users.updateProfile(user.getId(), "n".repeat(201), null));
        assertValidation(() -> users.updateProfile(user.getId(), "Valid", "123"));

        User renamed = users.updateProfile(user.getId(), " Updated ", null);
        assertEquals("Updated", renamed.getFullName());
        assertEquals("old-hash", renamed.getPasswordHash());
        assertEquals("Updated", users.updateProfile(user.getId(), "Updated", " ").getFullName());
        User passwordChanged = users.updateProfile(user.getId(), "Updated", "new-pass");
        assertNotNull(passwordChanged.getPasswordHash());
        assertFalse("old-hash".equals(passwordChanged.getPasswordHash()));

        assertValidation(() -> users.updatePhoto(user.getId(), null));
        assertValidation(() -> users.updatePhoto(user.getId(), " "));
        assertEquals("photo.png", users.updatePhoto(user.getId(), "photo.png").getPhotoPath());
        assertThrows(EntityNotFoundException.class, () -> users.updatePhoto(999, "photo.png"));
    }

    @Test
    void inMemoryRepositoriesSupportCrudAndFiltering() {
        UserRepository repository = new UserRepository();
        assertTrue(repository.findAll().isEmpty());
        assertTrue(repository.findById(1).isEmpty());
        assertFalse(repository.deleteById(1));

        User first = repository.saveNew("alice", "hash", "Alice", Role.USER);
        User second = new User(10, "bob", "hash", "Bob", Role.ADMIN, null);
        assertEquals(second, repository.save(second));
        assertEquals(2, repository.findAll().size());
        assertTrue(repository.findById(first.getId()).isPresent());
        assertEquals(first.getId(), repository.findByUsername("ALICE").orElseThrow().getId());
        assertTrue(repository.findByUsername("missing").isEmpty());
        assertEquals(1, repository.findBy(user -> user.getRole() == Role.ADMIN).size());
        assertTrue(repository.findBy(user -> user.getUsername().equals("missing")).isEmpty());

        second.changeFullName("Updated");
        repository.update(second);
        assertEquals("Updated", repository.findById(10).orElseThrow().getFullName());
        assertTrue(repository.deleteById(10));
        assertFalse(repository.deleteById(10));
    }

    @Test
    void entitiesExposeAndUpdateTheirState() {
        User user = new User(1, "name", "hash", "Full Name", Role.USER, null);
        assertEquals(1, user.getId());
        assertEquals("name", user.getUsername());
        assertEquals("hash", user.getPasswordHash());
        assertEquals("Full Name", user.getFullName());
        assertEquals(Role.USER, user.getRole());
        assertEquals(null, user.getPhotoPath());
        user.changePasswordHash("new-hash");
        user.changeFullName("New Name");
        user.changePhotoPath("new.png");
        assertEquals("new-hash", user.getPasswordHash());
        assertEquals("New Name", user.getFullName());
        assertEquals("new.png", user.getPhotoPath());

        Transaction transaction = transaction(5, 2, "Description", 42, TransactionType.EXPENSE, FIRST_DATE);
        assertEquals(5, transaction.getId());
        assertEquals(2, transaction.getUserId());
        assertEquals("Description", transaction.getDescription());
        assertEquals(42, transaction.getAmount());
        assertEquals(TransactionType.EXPENSE, transaction.getType());
        assertEquals(null, transaction.getProofPath());
        assertEquals(FIRST_DATE, transaction.getCreatedAt());
        transaction.update("Updated", 70, TransactionType.INCOME, "proof.jpg");
        assertEquals("Updated", transaction.getDescription());
        assertEquals(70, transaction.getAmount());
        assertEquals(TransactionType.INCOME, transaction.getType());
        assertEquals("proof.jpg", transaction.getProofPath());
    }

    private static Transaction transaction(int id, int userId, String description, long amount,
                                           TransactionType type, LocalDateTime createdAt) {
        return new Transaction(id, userId, description, amount, type, null, createdAt);
    }

    private static List<Integer> ids(List<Transaction> transactions) {
        return transactions.stream().map(Transaction::getId).toList();
    }

    private static void assertValidation(Executable action) {
        assertThrows(ValidationException.class, action);
    }
}
