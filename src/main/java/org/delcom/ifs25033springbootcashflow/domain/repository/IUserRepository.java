package org.delcom.ifs25033springbootcashflow.domain.repository;

import org.delcom.ifs25033springbootcashflow.domain.entity.Role;
import org.delcom.ifs25033springbootcashflow.domain.entity.User;

import java.util.Optional;

public interface IUserRepository extends IRepository<User, Integer> {
    Optional<User> findByUsername(String username);
    User saveNew(String username, String passwordHash, String fullName, Role role);
}
