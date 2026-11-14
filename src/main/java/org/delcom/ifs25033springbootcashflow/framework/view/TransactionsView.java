package org.delcom.ifs25033springbootcashflow.framework.view;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.delcom.ifs25033springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25033springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25033springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25033springbootcashflow.domain.entity.User;
import org.delcom.ifs25033springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25033springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25033springbootcashflow.usecase.FinanceUseCase;

@Route(value = "transactions", layout = MainLayout.class)
@PageTitle("Riwayat transaksi | Cashflow")
public class TransactionsView extends VerticalLayout {
    public TransactionsView(FinanceUseCase financeUseCase, FinancePresenter presenter,
                            IUserRepository userRepository) {
        addClassName("content-page");
        setPadding(true);
        add(new H1("Riwayat transaksi"));

        var currentUser = AuthSession.currentUser(userRepository);
        if (currentUser.isEmpty()) {
            add(new Span("Masuk untuk melihat riwayat transaksi."));
            add(new Anchor("login", "Masuk"));
            return;
        }

        User user = currentUser.orElseThrow();
        Grid<Transaction> grid = new Grid<>(Transaction.class, false);
        grid.addClassName("transaction-grid");
        grid.addColumn(Transaction::getId).setHeader("ID").setAutoWidth(true);
        grid.addColumn(presenter::formatDate).setHeader("Waktu").setAutoWidth(true);
        grid.addColumn(Transaction::getDescription).setHeader("Keterangan").setFlexGrow(1);
        grid.addColumn(transaction -> presenter.formatMoney(transaction.getAmount()))
                .setHeader("Jumlah").setAutoWidth(true);
        grid.addColumn(transaction -> transaction.getType() == TransactionType.INCOME
                ? "Pemasukan" : "Pengeluaran").setHeader("Tipe").setAutoWidth(true);
        grid.setWidthFull();
        grid.setItems(financeUseCase.getTransactions(user.getId()));
        add(grid);
    }
}
