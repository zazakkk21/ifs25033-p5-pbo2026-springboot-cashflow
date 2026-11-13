package org.delcom.ifs25033springbootcashflow;

import org.delcom.ifs25033springbootcashflow.adapter.repository.TransactionRepository;
import org.delcom.ifs25033springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25033springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25033springbootcashflow.usecase.FinanceUseCase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FinanceUseCaseTests {
    private final TransactionRepository repository = new TransactionRepository();
    private final FinanceUseCase useCase = new FinanceUseCase(repository);

    @Test
    void balanceIsScopedToUserAndAppliesExpenseAsNegative() throws Exception {
        useCase.addTransaction(1, "Gaji", 5_000_000, TransactionType.INCOME, null);
        useCase.addTransaction(1, "Belanja", 250_000, TransactionType.EXPENSE, null);
        useCase.addTransaction(2, "Pendapatan lain", 9_000_000, TransactionType.INCOME, null);

        assertEquals(4_750_000, useCase.getBalance(1).balance());
        assertEquals(5_000_000, useCase.getBalance(1).totalIncome());
        assertEquals(250_000, useCase.getBalance(1).totalExpense());
        assertEquals(2, useCase.getBalance(1).transactionCount());
    }

    @Test
    void rejectsBlankDescriptionAndNonPositiveAmounts() {
        assertThrows(ValidationException.class,
                () -> useCase.addTransaction(1, " ", 100, TransactionType.INCOME, null));
        assertThrows(ValidationException.class,
                () -> useCase.addTransaction(1, "Valid", 0, TransactionType.INCOME, null));
    }

    @Test
    void userCannotDeleteAnotherUsersTransaction() throws Exception {
        var transaction = useCase.addTransaction(2, "Pribadi", 100, TransactionType.EXPENSE, null);

        assertThrows(ValidationException.class, () -> useCase.removeTransaction(1, transaction.getId()));
        assertEquals(1, useCase.getTransactions(2).size());
    }
}
