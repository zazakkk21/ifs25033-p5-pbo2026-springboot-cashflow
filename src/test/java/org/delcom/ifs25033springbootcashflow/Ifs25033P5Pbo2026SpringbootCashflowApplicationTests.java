package org.delcom.ifs25033springbootcashflow;

import org.delcom.ifs25033springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25033springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25033springbootcashflow.usecase.AuthUseCase;
import org.delcom.ifs25033springbootcashflow.usecase.FinanceUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(properties = {
        "app.root-username=test-admin",
        "app.root-password=test-pass"
})
@ActiveProfiles("test")
class Ifs25033P5Pbo2026SpringbootCashflowApplicationTests {
    @Autowired
    private AuthUseCase authUseCase;

    @Autowired
    private FinanceUseCase financeUseCase;

    @Test
    void contextLoadsAndJdbcRepositoriesPersistTransactions() throws Exception {
        var user = authUseCase.login("test-admin", "test-pass");
        financeUseCase.addTransaction(user.getId(), "Pemasukan 100% uji", 100_000, TransactionType.INCOME, null);
        financeUseCase.addTransaction(user.getId(), "Pengeluaran uji", 25_000, TransactionType.EXPENSE, null);

        assertEquals(75_000, financeUseCase.getBalance(user.getId()).balance());
        assertEquals(1, financeUseCase.countTransactions(user.getId(), "100%"));
        assertEquals(1, financeUseCase.searchTransactions(
                user.getId(), "100%", SortOption.NEWEST, 0, 30).size());
    }
}
