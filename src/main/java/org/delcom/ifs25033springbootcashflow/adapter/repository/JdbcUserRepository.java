package org.delcom.ifs25033springbootcashflow.adapter.repository;

import org.delcom.ifs25033springbootcashflow.domain.entity.Role;
import org.delcom.ifs25033springbootcashflow.domain.entity.User;
import org.delcom.ifs25033springbootcashflow.domain.repository.IUserRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

public class JdbcUserRepository implements IUserRepository {
    private static final RowMapper<User> USER_ROW_MAPPER = (row, index) -> new User(
            row.getInt("id"),
            row.getString("username"),
            row.getString("password_hash"),
            row.getString("full_name"),
            Role.valueOf(row.getString("role")),
            row.getString("photo_path"));
    private final JdbcTemplate jdbc;

    public JdbcUserRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override public List<User> findAll() {
        return jdbc.query("SELECT * FROM users ORDER BY id", USER_ROW_MAPPER);
    }

    @Override public Optional<User> findById(Integer id) {
        return jdbc.query("SELECT * FROM users WHERE id = ?", USER_ROW_MAPPER, id).stream().findFirst();
    }

    @Override public Optional<User> findByUsername(String username) {
        return jdbc.query("SELECT * FROM users WHERE LOWER(username) = LOWER(?)",
                USER_ROW_MAPPER, username).stream().findFirst();
    }

    @Override public User save(User user) {
        jdbc.update("INSERT INTO users (id, username, password_hash, full_name, role, photo_path) "
                        + "VALUES (?, ?, ?, ?, ?, ?)",
                user.getId(), user.getUsername(), user.getPasswordHash(), user.getFullName(),
                user.getRole().name(), user.getPhotoPath());
        return user;
    }

    @Override public User saveNew(String username, String passwordHash, String fullName, Role role) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO users (username, password_hash, full_name, role) VALUES (?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, username);
            statement.setString(2, passwordHash);
            statement.setString(3, fullName);
            statement.setString(4, role.name());
            return statement;
        }, keyHolder);
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("Database tidak mengembalikan ID pengguna");
        }
        return new User(key.intValue(), username, passwordHash, fullName, role, null);
    }

    @Override public boolean deleteById(Integer id) {
        return jdbc.update("DELETE FROM users WHERE id = ?", id) > 0;
    }

    @Override public void update(User user) {
        jdbc.update("UPDATE users SET password_hash = ?, full_name = ?, role = ?, photo_path = ? WHERE id = ?",
                user.getPasswordHash(), user.getFullName(), user.getRole().name(), user.getPhotoPath(), user.getId());
    }

    @Override public List<User> findBy(java.util.function.Predicate<User> predicate) {
        return findAll().stream().filter(predicate).toList();
    }
}
