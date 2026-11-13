package org.delcom.ifs25033springbootcashflow.usecase;

import org.delcom.ifs25033springbootcashflow.domain.PasswordHasher;
import org.delcom.ifs25033springbootcashflow.domain.entity.Role;
import org.delcom.ifs25033springbootcashflow.domain.entity.User;
import org.delcom.ifs25033springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25033springbootcashflow.domain.repository.IUserRepository;

public class AuthUseCase {
    private final IUserRepository userRepository;

    public AuthUseCase(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User register(String username, String password, String fullName) throws ValidationException {
        validateCredentials(username, password, fullName);
        String normalizedUsername = username.trim();
        if (userRepository.findByUsername(normalizedUsername).isPresent()) {
            throw new ValidationException("Username sudah dipakai");
        }
        return userRepository.saveNew(normalizedUsername, PasswordHasher.hash(password), fullName.trim(), Role.USER);
    }

    public User login(String username, String password) throws ValidationException {
        if (username == null || username.isBlank() || password == null || password.isEmpty()) {
            throw new ValidationException("Username atau password salah");
        }
        User user = userRepository.findByUsername(username.trim())
                .orElseThrow(() -> new ValidationException("Username atau password salah"));
        if (!PasswordHasher.matches(password, user.getPasswordHash())) {
            throw new ValidationException("Username atau password salah");
        }
        return user;
    }

    public User ensureRootAdmin(String username, String password) throws ValidationException {
        if (username == null || username.isBlank() || password == null || password.length() < 4) {
            throw new ValidationException("ROOT_USERNAME dan ROOT_PASSWORD harus diisi (password minimal 4 karakter)");
        }
        return userRepository.findByUsername(username.trim()).orElseGet(() ->
                userRepository.saveNew(username.trim(), PasswordHasher.hash(password), "Administrator", Role.ADMIN));
    }

    private void validateCredentials(String username, String password, String fullName) throws ValidationException {
        if (username == null || username.isBlank() || username.trim().length() > 100) {
            throw new ValidationException("Username wajib diisi (maksimal 100 karakter)");
        }
        if (password == null || password.length() < 4) {
            throw new ValidationException("Password minimal 4 karakter");
        }
        if (fullName == null || fullName.isBlank() || fullName.trim().length() > 200) {
            throw new ValidationException("Nama lengkap wajib diisi (maksimal 200 karakter)");
        }
    }
}
