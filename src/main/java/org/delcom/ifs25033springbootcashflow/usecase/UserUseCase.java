package org.delcom.ifs25033springbootcashflow.usecase;

import org.delcom.ifs25033springbootcashflow.domain.PasswordHasher;
import org.delcom.ifs25033springbootcashflow.domain.entity.User;
import org.delcom.ifs25033springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25033springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25033springbootcashflow.domain.repository.IUserRepository;

import java.util.List;

public class UserUseCase {
    private final IUserRepository userRepository;

    public UserUseCase(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> listUsers() {
        return userRepository.findAll();
    }

    public User getUser(int id) throws EntityNotFoundException {
        return userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(id));
    }

    public User updateProfile(int userId, String fullName, String newPassword)
            throws EntityNotFoundException, ValidationException {
        User user = getUser(userId);
        if (fullName == null || fullName.isBlank() || fullName.trim().length() > 200) {
            throw new ValidationException("Nama lengkap wajib diisi (maksimal 200 karakter)");
        }
        if (newPassword != null && !newPassword.isBlank()) {
            if (newPassword.length() < 4) {
                throw new ValidationException("Password minimal 4 karakter");
            }
            user.changePasswordHash(PasswordHasher.hash(newPassword));
        }
        user.changeFullName(fullName.trim());
        userRepository.update(user);
        return user;
    }

    public User updatePhoto(int userId, String photoPath) throws EntityNotFoundException, ValidationException {
        if (photoPath == null || photoPath.isBlank()) {
            throw new ValidationException("Path foto tidak boleh kosong");
        }
        User user = getUser(userId);
        user.changePhotoPath(photoPath);
        userRepository.update(user);
        return user;
    }
}
