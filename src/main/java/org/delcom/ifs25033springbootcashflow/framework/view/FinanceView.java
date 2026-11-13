package org.delcom.ifs25033springbootcashflow.framework.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import org.delcom.ifs25033springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25033springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25033springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25033springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25033springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25033springbootcashflow.domain.entity.TransactionSummary;
import org.delcom.ifs25033springbootcashflow.domain.entity.User;
import org.delcom.ifs25033springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25033springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25033springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25033springbootcashflow.usecase.FinanceUseCase;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Dashboard | Cashflow")
public class FinanceView extends VerticalLayout {
    private static final int PAGE_SIZE = 30;
    private final FinanceUseCase financeUseCase;
    private final FinancePresenter presenter;
    private final FileStorage fileStorage;
    private final User user;
    private final Grid<Transaction> grid = new Grid<>(Transaction.class, false);
    private final TextField search = new TextField("Cari keterangan");
    private final ComboBox<SortOption> sort = new ComboBox<>("Urutkan");
    private final Span total = new Span();
    private final Span income = new Span();
    private final Span expense = new Span();
    private final Span balance = new Span();

    public FinanceView(FinanceUseCase financeUseCase, FinancePresenter presenter, FileStorage fileStorage,
                       IUserRepository userRepository) {
        this.financeUseCase = financeUseCase;
        this.presenter = presenter;
        this.fileStorage = fileStorage;
        this.user = AuthSession.currentUser(userRepository).orElseThrow();
        addClassName("finance-page");
        setPadding(true);
        setSpacing(true);
        Div heading = new Div();
        heading.addClassName("page-heading");
        heading.add(new H3("Ringkasan keuangan"), new Span("Pantau arus kas dan catat transaksi harian Anda."));
        HorizontalLayout summary = new HorizontalLayout(
                summaryCard("Transaksi", total),
                summaryCard("Pemasukan", income),
                summaryCard("Pengeluaran", expense),
                summaryCard("Saldo", balance));
        summary.addClassName("summary-grid");
        summary.setWidthFull();
        summary.getChildren().forEach(card -> summary.setFlexGrow(1, card));
        Button addIncome = new Button("Tambah pemasukan", event -> openTransactionDialog(TransactionType.INCOME));
        addIncome.addClassName("action-income");
        Button addExpense = new Button("Tambah pengeluaran", event -> openTransactionDialog(TransactionType.EXPENSE));
        addExpense.addClassName("action-expense");
        Button reload = new Button("Muat ulang", event -> refresh());
        reload.addClassName("action-refresh");
        search.setPlaceholder("Contoh: makan siang");
        search.setWidth("min(100%, 22rem)");
        search.setClearButtonVisible(true);
        search.setValueChangeMode(com.vaadin.flow.data.value.ValueChangeMode.LAZY);
        search.setValueChangeTimeout(300);
        search.addValueChangeListener(event -> refreshGrid());
        sort.setItems(SortOption.values());
        sort.setItemLabelGenerator(this::sortLabel);
        sort.setValue(SortOption.NEWEST);
        sort.setWidth("12rem");
        sort.addValueChangeListener(event -> refreshGrid());
        HorizontalLayout toolbar = new HorizontalLayout(search, sort, reload, addIncome, addExpense);
        toolbar.addClassName("finance-toolbar");
        toolbar.setWidthFull();
        toolbar.getStyle().set("flex-wrap", "wrap");
        toolbar.setAlignItems(Alignment.END);
        configureGrid();
        add(heading, summary, toolbar, grid);
        refresh();
    }

    private VerticalLayout summaryCard(String title, Span value) {
        Span label = new Span(title);
        label.addClassName("summary-label");
        value.addClassName("summary-value");
        VerticalLayout card = new VerticalLayout(label, value);
        card.addClassName("summary-card");
        card.setPadding(true);
        card.setSpacing(false);
        return card;
    }

    private void configureGrid() {
        grid.addClassName("transaction-grid");
        grid.setPageSize(PAGE_SIZE);
        grid.addColumn(Transaction::getId).setHeader("ID").setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(presenter::formatDate).setHeader("Waktu").setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(Transaction::getDescription).setHeader("Keterangan").setFlexGrow(1);
        grid.addColumn(transaction -> presenter.formatMoney(transaction.getAmount()))
                .setHeader("Jumlah").setAutoWidth(true).setFlexGrow(0);
        grid.addComponentColumn(transaction -> {
            Span badge = new Span(transaction.getType() == TransactionType.INCOME ? "Pemasukan" : "Pengeluaran");
            badge.addClassName(transaction.getType() == TransactionType.INCOME ? "type-income" : "type-expense");
            return badge;
        }).setHeader("Tipe").setAutoWidth(true).setFlexGrow(0);
        grid.addComponentColumn(transaction -> {
            if (transaction.getProofPath() == null || transaction.getProofPath().isBlank()) {
                return new Span("-");
            }
            Image thumbnail = new Image("/uploads/" + transaction.getProofPath(), "Bukti transaksi");
            thumbnail.setWidth("48px");
            thumbnail.setHeight("48px");
            thumbnail.getStyle().set("object-fit", "cover").set("cursor", "pointer");
            Button preview = new Button(thumbnail,
                    event -> new ImagePreviewDialog("/uploads/" + transaction.getProofPath()).open());
            preview.addClassName("proof-preview");
            return preview;
        }).setHeader("Bukti").setAutoWidth(true);
        grid.addComponentColumn(transaction -> {
            Button delete = new Button("Hapus", event -> confirmDelete(transaction));
            delete.addClassName("delete-action");
            return delete;
        })
                .setHeader("Tindakan").setAutoWidth(true);
        grid.setWidthFull();
        grid.setItems(query -> financeUseCase.searchTransactions(user.getId(), search.getValue(),
                        sort.getValue(), query.getOffset(), query.getLimit()).stream(),
                query -> financeUseCase.countTransactions(user.getId(), search.getValue()));
    }

    private void refresh() {
        TransactionSummary summary = financeUseCase.getBalance(user.getId());
        total.setText(Integer.toString(summary.transactionCount()));
        income.setText(presenter.formatMoney(summary.totalIncome()));
        expense.setText(presenter.formatMoney(summary.totalExpense()));
        balance.setText(presenter.formatMoney(summary.balance()));
        grid.getDataProvider().refreshAll();
    }

    private void refreshGrid() {
        grid.getDataProvider().refreshAll();
    }

    private void openTransactionDialog(TransactionType type) {
        Dialog dialog = new Dialog();
        dialog.addClassName("cashflow-dialog");
        dialog.setHeaderTitle(type == TransactionType.INCOME ? "Tambah pemasukan" : "Tambah pengeluaran");
        TextArea description = new TextArea("Keterangan");
        description.setMaxLength(2000);
        description.setWidthFull();
        description.setPlaceholder("Contoh: Gaji bulanan");
        description.setRequiredIndicatorVisible(true);
        TextField amount = new TextField("Jumlah (angka bulat)");
        amount.setAllowedCharPattern("[0-9]");
        amount.setPlaceholder("Contoh: 1500000");
        amount.setPrefixComponent(new Span("Rp"));
        amount.setRequiredIndicatorVisible(true);
        MemoryBuffer buffer = new MemoryBuffer();
        Upload upload = new Upload(buffer);
        upload.setAcceptedFileTypes("image/png", "image/jpeg", "image/gif", "image/webp");
        upload.setMaxFiles(1);
        upload.setMaxFileSize(5 * 1024 * 1024);
        upload.setDropLabel(new Span("Bukti transaksi (opsional, maks. 5 MB)"));
        AtomicBoolean proofUploaded = new AtomicBoolean(false);
        upload.addSucceededListener(event -> proofUploaded.set(true));
        Button cancel = new Button("Batal", event -> dialog.close());
        cancel.addClassName("dialog-cancel");
        Button save = new Button("Simpan", event -> {
            String savedPath = null;
            try {
                long numericAmount = Long.parseLong(amount.getValue());
                if (proofUploaded.get()) {
                    String fileName = buffer.getFileName();
                    if (fileName == null || fileName.isBlank()) {
                        throw new IllegalStateException("Nama file bukti tidak tersedia setelah upload");
                    }
                    savedPath = fileStorage.store(buffer.getInputStream(), fileName);
                }
                financeUseCase.addTransaction(user.getId(), description.getValue(), numericAmount, type, savedPath);
                Notification.show("Transaksi berhasil disimpan");
                dialog.close();
                refresh();
            } catch (NumberFormatException exception) {
                Notification.show("Masukkan jumlah positif dalam angka bulat");
            } catch (DomainException exception) {
                deleteIfPresent(savedPath);
                Notification.show(exception.getMessage());
            } catch (IOException | IllegalArgumentException exception) {
                deleteIfPresent(savedPath);
                Notification.show("Gagal menyimpan bukti: " + exception.getMessage());
            }
        });
        save.addClassName(type == TransactionType.INCOME ? "action-income" : "action-expense");
        HorizontalLayout footer = new HorizontalLayout(cancel, save);
        footer.setWidthFull();
        footer.setJustifyContentMode(JustifyContentMode.END);
        dialog.add(description, amount, upload);
        dialog.getFooter().add(footer);
        dialog.open();
    }

    private void confirmDelete(Transaction transaction) {
        ConfirmDialog confirmation = new ConfirmDialog();
        confirmation.setHeader("Hapus transaksi?");
        confirmation.setText("Transaksi \"" + transaction.getDescription() + "\" akan dihapus.");
        confirmation.setCancelable(true);
        confirmation.setCancelText("Batal");
        confirmation.setConfirmText("Hapus");
        confirmation.addConfirmListener(event -> {
            try {
                financeUseCase.removeTransaction(user.getId(), transaction.getId());
                if (transaction.getProofPath() != null) {
                    fileStorage.delete(transaction.getProofPath());
                }
                Notification.show("Transaksi dihapus");
                refresh();
            } catch (DomainException exception) {
                Notification.show(exception.getMessage());
            } catch (IOException | IllegalArgumentException exception) {
                Notification.show("Transaksi terhapus, tetapi file bukti gagal dibersihkan: " + exception.getMessage());
            }
        });
        confirmation.open();
    }

    private String sortLabel(SortOption option) {
        return switch (option) {
            case NEWEST -> "Terbaru";
            case OLDEST -> "Terlama";
            case AMOUNT_ASC -> "Jumlah terkecil";
            case AMOUNT_DESC -> "Jumlah terbesar";
            case INCOME_FIRST -> "Pemasukan dahulu";
            case EXPENSE_FIRST -> "Pengeluaran dahulu";
        };
    }

    private void deleteIfPresent(String path) {
        if (path == null) {
            return;
        }
        try {
            fileStorage.delete(path);
        } catch (IOException exception) {
            Notification.show("Berkas sementara gagal dibersihkan: " + exception.getMessage());
        }
    }
}
