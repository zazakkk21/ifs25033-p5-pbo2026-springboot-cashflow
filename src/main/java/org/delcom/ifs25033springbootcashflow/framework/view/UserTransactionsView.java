package org.delcom.ifs25033springbootcashflow.framework.view;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.delcom.ifs25033springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25033springbootcashflow.domain.entity.Role;
import org.delcom.ifs25033springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25033springbootcashflow.domain.entity.User;
import org.delcom.ifs25033springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25033springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25033springbootcashflow.usecase.FinanceUseCase;
import org.delcom.ifs25033springbootcashflow.usecase.UserUseCase;

import java.util.List;

@Route(value = "user-transactions", layout = MainLayout.class)
@PageTitle("Transaksi pengguna | Cashflow")
public class UserTransactionsView extends VerticalLayout implements HasUrlParameter<String>, BeforeEnterObserver {
    private final IUserRepository userRepository;
    private final UserUseCase userUseCase;
    private final FinanceUseCase financeUseCase;
    private final FinancePresenter presenter;
    private final H1 heading = new H1("Transaksi pengguna");

    public UserTransactionsView(IUserRepository userRepository, UserUseCase userUseCase,
                                FinanceUseCase financeUseCase, FinancePresenter presenter) {
        this.userRepository = userRepository;
        this.userUseCase = userUseCase;
        this.financeUseCase = financeUseCase;
        this.presenter = presenter;
        setPadding(true);
        heading.addClassName("user-transactions-heading");
        add(heading);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (AuthSession.currentUser(userRepository).map(user -> user.getRole() != Role.ADMIN).orElse(true)) {
            event.forwardTo("");
        }
    }

    @Override
    public void setParameter(BeforeEvent event, String parameter) {
        try {
            int userId = Integer.parseInt(parameter);
            User user = userUseCase.getUser(userId);
            heading.setText("Transaksi: " + user.getFullName() + " (read-only)");
            Grid<Transaction> grid = new Grid<>(Transaction.class, false);
            grid.addColumn(Transaction::getId).setHeader("ID").setAutoWidth(true);
            grid.addColumn(presenter::formatDate).setHeader("Waktu");
            grid.addColumn(Transaction::getDescription).setHeader("Keterangan").setFlexGrow(1);
            grid.addColumn(transaction -> presenter.formatMoney(transaction.getAmount())).setHeader("Jumlah");
            grid.addColumn(transaction -> transaction.getType().name()).setHeader("Tipe");
            grid.addComponentColumn(transaction -> transaction.getProofPath() == null
                    ? new Span("-")
                    : new com.vaadin.flow.component.button.Button("Lihat bukti",
                    click -> new ImagePreviewDialog("/uploads/" + transaction.getProofPath()).open()))
                    .setHeader("Bukti");
            List<Transaction> transactions = financeUseCase.getTransactions(userId);
            grid.setItems(transactions);
            grid.setWidthFull();
            add(grid);
        } catch (NumberFormatException exception) {
            add(new Span("ID pengguna tidak valid"));
        } catch (org.delcom.ifs25033springbootcashflow.domain.exception.EntityNotFoundException exception) {
            add(new Span(exception.getMessage()));
        }
    }
}
