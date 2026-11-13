package org.delcom.ifs25033springbootcashflow;

import org.delcom.ifs25033springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25033springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25033springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25033springbootcashflow.domain.entity.TransactionSummary;
import org.delcom.ifs25033springbootcashflow.domain.entity.TransactionType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StorageAndPresenterTests {
    @TempDir
    Path tempDir;

    @Test
    void fileStoragePersistsSupportedImagesAndDeletesSafely() throws Exception {
        FileStorage storage = new FileStorage(tempDir.resolve("uploads").toString());
        assertTrue(storage.root().isAbsolute());
        assertTrue(storage.root().endsWith("uploads"));

        for (String extension : List.of(".png", ".jpg", ".jpeg", ".gif", ".webp")) {
            String stored = storage.store(new ByteArrayInputStream(new byte[] {1, 2, 3}),
                    "photo" + extension.toUpperCase());
            assertTrue(stored.endsWith(extension));
            assertTrue(Files.exists(storage.root().resolve(stored)));
            storage.delete(stored);
            assertFalse(Files.exists(storage.root().resolve(stored)));
        }

        storage.delete(null);
        storage.delete(" ");
        storage.delete("missing.png");
        assertThrows(IllegalArgumentException.class,
                () -> storage.delete("../outside.png"));
    }

    @Test
    void fileStorageRejectsMissingAndUnsupportedExtensions() {
        FileStorage storage = new FileStorage(tempDir.resolve("uploads").toString());
        assertThrows(IllegalArgumentException.class,
                () -> storage.store(new ByteArrayInputStream(new byte[0]), null));
        assertThrows(IllegalArgumentException.class,
                () -> storage.store(new ByteArrayInputStream(new byte[0]), "no-extension"));
        assertThrows(IllegalArgumentException.class,
                () -> storage.store(new ByteArrayInputStream(new byte[0]), "document.pdf"));
    }

    @Test
    void financePresenterFormatsMoneyTransactionsBalanceAndDates() {
        FinancePresenter presenter = new FinancePresenter();
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 2, 3, 4);
        Transaction income = new Transaction(12, 1, "Gaji", 1_234_500,
                TransactionType.INCOME, null, createdAt);
        Transaction expense = new Transaction(13, 1, "Makan", 25_000,
                TransactionType.EXPENSE, null, createdAt);

        assertEquals("Rp 1,234,500.00", presenter.formatMoney(1_234_500));
        assertEquals("[12] Gaji | Rp 1,234,500.00 | Pemasukan", presenter.formatTransaction(income));
        assertEquals("[13] Makan | Rp 25,000.00 | Pengeluaran", presenter.formatTransaction(expense));
        assertEquals("Saldo: Rp 1,209,500.00",
                presenter.formatBalance(new TransactionSummary(1_234_500, 25_000, 1_209_500, 2)));
        assertEquals("02 Jan 2026, 03:04", presenter.formatDate(income));
    }
}
