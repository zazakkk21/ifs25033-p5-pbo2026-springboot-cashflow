package org.delcom.ifs25033springbootcashflow.adapter.repository;

import org.delcom.ifs25033springbootcashflow.domain.entity.Role;
import org.delcom.ifs25033springbootcashflow.domain.entity.User;
import org.delcom.ifs25033springbootcashflow.domain.repository.IUserRepository;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class UserRepository extends InMemoryRepository<User, Integer> implements IUserRepository {
    private final AtomicInteger nextId = new AtomicInteger(1);

    public UserRepository() {
        super(User::getId);
    }

    @Override public Optional<User> findByUsername(String username) {
        return findBy(user -> user.getUsername().equalsIgnoreCase(username)).stream().findFirst();
    }

    @Override public User saveNew(String username, String passwordHash, String fullName, Role role) {
        User user = new User(nextId.getAndIncrement(), username, passwordHash, fullName, role, null);
        return save(user);
    }
}
