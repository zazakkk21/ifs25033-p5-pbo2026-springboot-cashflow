package org.delcom.ifs25033springbootcashflow.framework.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.delcom.ifs25033springbootcashflow.domain.entity.Role;
import org.delcom.ifs25033springbootcashflow.domain.entity.User;
import org.delcom.ifs25033springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25033springbootcashflow.framework.auth.AuthSession;

@Route(value = "users", layout = MainLayout.class)
@PageTitle("Pengguna | Cashflow")
public class UsersView extends VerticalLayout implements BeforeEnterObserver {
    private final IUserRepository userRepository;

    public UsersView(IUserRepository userRepository) {
        this.userRepository = userRepository;
        addClassName("content-page");
        setPadding(true);
        add(new H1("Daftar pengguna"));
        Grid<User> grid = new Grid<>(User.class, false);
        grid.addClassName("transaction-grid");
        grid.addColumn(User::getId).setHeader("ID").setAutoWidth(true);
        grid.addColumn(User::getUsername).setHeader("Username");
        grid.addColumn(User::getFullName).setHeader("Nama lengkap");
        grid.addColumn(user -> user.getRole().name()).setHeader("Role");
        grid.addComponentColumn(user -> new Button("Lihat transaksi",
                event -> getUI().ifPresent(ui -> ui.navigate("user-transactions/" + user.getId()))))
                .setHeader("Aksi");
        grid.setItems(userRepository.findAll());
        grid.setWidthFull();
        add(grid);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (AuthSession.currentUser(userRepository).map(user -> user.getRole() != Role.ADMIN).orElse(true)) {
            event.forwardTo("");
        }
    }
}
