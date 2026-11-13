package org.delcom.ifs25033springbootcashflow.framework.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.RouterLink;
import org.delcom.ifs25033springbootcashflow.domain.entity.Role;
import org.delcom.ifs25033springbootcashflow.domain.entity.User;
import org.delcom.ifs25033springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25033springbootcashflow.framework.auth.AuthSession;

public class MainLayout extends AppLayout implements BeforeEnterObserver {
    private final IUserRepository userRepository;

    public MainLayout(IUserRepository userRepository) {
        this.userRepository = userRepository;
        addClassName("cashflow-layout");
        buildLayout();
    }

    private void buildLayout() {
        VerticalLayout sidebar = new VerticalLayout();
        sidebar.addClassName("cashflow-sidebar");
        sidebar.setSizeFull();
        sidebar.setPadding(true);
        sidebar.setSpacing(true);
        H2 logo = new H2("Cashflow");
        logo.addClassName("brand-mark");
        sidebar.add(logo);
        RouterLink dashboard = new RouterLink("Dashboard", FinanceView.class);
        dashboard.addClassName("sidebar-link");
        RouterLink transactions = new RouterLink("Riwayat transaksi", TransactionsView.class);
        transactions.addClassName("sidebar-link");
        RouterLink profile = new RouterLink("Profil", ProfileView.class);
        profile.addClassName("sidebar-link");
        sidebar.add(dashboard, transactions, profile);
        var currentUser = AuthSession.currentUser(userRepository);
        currentUser.filter(user -> user.getRole() == Role.ADMIN).ifPresent(user -> {
            RouterLink users = new RouterLink("Pengguna", UsersView.class);
            users.addClassName("sidebar-link");
            sidebar.add(users);
        });
        Span caption = new Span("Kelola pemasukan dan pengeluaran");
        caption.addClassName("sidebar-caption");
        sidebar.add(caption);
        addToDrawer(sidebar);
        currentUser.ifPresent(user -> {
            Avatar avatar = new Avatar(user.getFullName());
            avatar.addClassName("user-avatar");
            if (user.getPhotoPath() != null) {
                avatar.setImage("/uploads/" + user.getPhotoPath());
            }
            RouterLink profileAvatar = new RouterLink(ProfileView.class);
            profileAvatar.addClassName("profile-avatar-link");
            profileAvatar.getElement().setAttribute("aria-label", "Buka profil");
            profileAvatar.add(avatar);
            Button logout = new Button("Keluar", event -> {
                AuthSession.logout();
                UI.getCurrent().navigate("login");
            });
            logout.addClassName("logout-button");
            Span name = new Span(user.getFullName());
            name.addClassName("navbar-user-name");
            HorizontalLayout userMenu = new HorizontalLayout(profileAvatar, name, logout);
            userMenu.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
            userMenu.addClassName("navbar-user-menu");
            addToNavbar(userMenu);
        });
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (AuthSession.currentUser(userRepository).isEmpty()) {
            event.forwardTo("login");
        }
    }

    public static boolean requireAdmin(IUserRepository repository) {
        return AuthSession.currentUser(repository).map(user -> user.getRole() == Role.ADMIN).orElse(false);
    }

    public static User requireUser(IUserRepository repository) {
        return AuthSession.currentUser(repository).orElse(null);
    }
}
