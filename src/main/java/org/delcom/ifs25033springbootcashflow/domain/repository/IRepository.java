package org.delcom.ifs25033springbootcashflow.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public interface IRepository<T, ID> {
    List<T> findAll();
    Optional<T> findById(ID id);
    T save(T entity);
    boolean deleteById(ID id);
    void update(T entity);
    List<T> findBy(Predicate<T> predicate);
}
