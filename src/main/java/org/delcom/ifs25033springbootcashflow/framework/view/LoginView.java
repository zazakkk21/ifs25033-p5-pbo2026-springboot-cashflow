package org.delcom.ifs25033springbootcashflow.framework.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
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

@Route("login")
@PageTitle("Masuk | Cashflow")
public class LoginView extends VerticalLayout implements BeforeEnterObserver {
    private final IUserRepository userRepository;

    public LoginView(AuthUseCase authUseCase, IUserRepository userRepository) {
        this.userRepository = userRepository;
        setSizeFull();
        addClassName("auth-page");
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        Div card = new Div();
        card.addClassName("auth-card");
        H2 heading = new H2("Selamat datang");
        Span subtitle = new Span("Masuk untuk melihat dan mengelola catatan keuangan Anda.");
        subtitle.addClassName("auth-subtitle");
        card.add(heading, subtitle);
        TextField username = new TextField("Username");
        username.getElement().executeJs("this.inputElement.id = $0", "login-username");
        username.setWidthFull();
        PasswordField password = new PasswordField("Password");
        password.getElement().executeJs("this.inputElement.id = $0", "login-password");
        password.setWidthFull();
        Button submit = new Button("Masuk", event -> {
            try {
                AuthSession.login(authUseCase.login(username.getValue(), password.getValue()));
                UI.getCurrent().navigate("");
            } catch (DomainException exception) {
                Notification.show(exception.getMessage());
            }
        });
        submit.setId("login-submit");
        submit.setWidth("100%");
        submit.addClassName("action-income");
        Anchor register = new Anchor("register", "Belum punya akun? Daftar");
        register.addClassName("auth-link");
        card.add(username, password, submit, register);
        add(card);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (AuthSession.currentUser(userRepository).isPresent()) {
            event.forwardTo("");
        }
    }
}
