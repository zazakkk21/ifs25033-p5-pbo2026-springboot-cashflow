package org.delcom.ifs25033springbootcashflow.framework.auth;

import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.WrappedSession;
import jakarta.servlet.http.Cookie;
import org.delcom.ifs25033springbootcashflow.domain.entity.User;
import org.delcom.ifs25033springbootcashflow.domain.repository.IUserRepository;

import java.util.Optional;

public final class AuthSession {
    public static final String LOGGED_OUT_KEY = "authLoggedOut";
    private static final String USER_KEY = "cashflowUser";
    private static final String USER_ID_KEY = "cashflowUserId";
    private static final String COOKIE_NAME = "cashflow_uid";

    private AuthSession() {
    }

    public static void login(User user) {
        VaadinSession session = VaadinSession.getCurrent();
        if (session == null) {
            return;
        }
        session.setAttribute(LOGGED_OUT_KEY, Boolean.FALSE);
        session.setAttribute(USER_KEY, user);
        session.setAttribute(USER_ID_KEY, user.getId());
        WrappedSession httpSession = session.getSession();
        if (httpSession != null) {
            httpSession.setAttribute(LOGGED_OUT_KEY, Boolean.FALSE);
            httpSession.setAttribute(USER_ID_KEY, user.getId());
        }
        writeCookie(user.getId());
    }

    public static void logout() {
        VaadinSession session = VaadinSession.getCurrent();
        if (session != null) {
            session.setAttribute(USER_KEY, null);
            session.setAttribute(USER_ID_KEY, null);
            session.setAttribute(LOGGED_OUT_KEY, Boolean.TRUE);
            WrappedSession httpSession = session.getSession();
            if (httpSession != null) {
                httpSession.setAttribute(USER_ID_KEY, null);
                httpSession.setAttribute(LOGGED_OUT_KEY, Boolean.TRUE);
            }
        }
        clearCookie();
    }

    public static Optional<User> currentUser(IUserRepository repository) {
        VaadinSession session = VaadinSession.getCurrent();
        if (session == null || isLoggedOut(session)) {
            return Optional.empty();
        }
        Object existing = session.getAttribute(USER_KEY);
        if (existing instanceof User user) {
            return Optional.of(user);
        }
        Integer userId = storedUserId(session);
        if (userId == null) {
            return Optional.empty();
        }
        Optional<User> user = repository.findById(userId);
        user.ifPresent(AuthSession::login);
        if (user.isEmpty()) {
            clearCookie();
        }
        return user;
    }

    public static void refresh(User user) {
        login(user);
    }

    private static boolean isLoggedOut(VaadinSession session) {
        if (Boolean.TRUE.equals(session.getAttribute(LOGGED_OUT_KEY))) {
            return true;
        }
        WrappedSession httpSession = session.getSession();
        return httpSession != null && Boolean.TRUE.equals(httpSession.getAttribute(LOGGED_OUT_KEY));
    }

    private static Integer storedUserId(VaadinSession session) {
        Object value = session.getAttribute(USER_ID_KEY);
        if (value instanceof Integer id) {
            return id;
        }
        WrappedSession httpSession = session.getSession();
        if (httpSession != null && httpSession.getAttribute(USER_ID_KEY) instanceof Integer id) {
            return id;
        }
        VaadinRequest request = VaadinService.getCurrentRequest();
        if (request != null && request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (COOKIE_NAME.equals(cookie.getName())) {
                    try {
                        return Integer.parseInt(cookie.getValue());
                    } catch (NumberFormatException exception) {
                        return null;
                    }
                }
            }
        }
        return null;
    }

    private static void writeCookie(int id) {
        VaadinResponse response = VaadinService.getCurrentResponse();
        if (response == null) {
            return;
        }
        Cookie cookie = new Cookie(COOKIE_NAME, Integer.toString(id));
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(60 * 60 * 24 * 7);
        response.addCookie(cookie);
    }

    private static void clearCookie() {
        VaadinResponse response = VaadinService.getCurrentResponse();
        if (response != null) {
            Cookie cookie = new Cookie(COOKIE_NAME, "");
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            cookie.setMaxAge(0);
            response.addCookie(cookie);
        }
    }
}
