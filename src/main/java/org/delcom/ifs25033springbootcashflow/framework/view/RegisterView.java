package org.delcom.ifs25033springbootcashflow.framework.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.delcom.ifs25033springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25033springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25033springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25033springbootcashflow.usecase.AuthUseCase;

@Route("register")
@PageTitle("Daftar | Cashflow")
public class RegisterView extends VerticalLayout implements BeforeEnterObserver {
    private final IUserRepository userRepository;

    public RegisterView(AuthUseCase authUseCase, IUserRepository userRepository) {
        this.userRepository = userRepository;
        setSizeFull();
        addClassName("auth-page");
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        Main main = new Main();
        main.addClassName("auth-main");
        Div card = new Div();
        card.addClassName("auth-card");
        card.add(new H1("Buat akun Cashflow"));
        Span subtitle = new Span("Mulai catat pemasukan dan pengeluaran Anda dengan rapi.");
        subtitle.addClassName("auth-subtitle");
        card.add(subtitle);
        TextField fullName = new TextField("Nama lengkap");
        fullName.setWidthFull();
        TextField username = new TextField("Username");
        username.setWidthFull();
        PasswordField password = new PasswordField("Password");
        password.setWidthFull();
        Button submit = new Button("Daftar", event -> {
            try {
                AuthSession.login(authUseCase.register(username.getValue(), password.getValue(), fullName.getValue()));
                UI.getCurrent().navigate("");
            } catch (DomainException exception) {
                Notification.show(exception.getMessage());
            }
        });
        submit.setWidthFull();
        submit.addClassName("action-income");
        Anchor login = new Anchor("login", "Sudah punya akun? Masuk");
        login.addClassName("auth-link");
        card.add(fullName, username, password, submit, login);
        main.add(card);
        add(main);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (AuthSession.currentUser(userRepository).isPresent()) {
            event.forwardTo("");
        }
    }
}
