package org.delcom.ifs25033springbootcashflow.adapter.repository;

import org.delcom.ifs25033springbootcashflow.domain.repository.IRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public abstract class InMemoryRepository<T, ID> implements IRepository<T, ID> {
    protected final Map<ID, T> entities = new LinkedHashMap<>();
    private final Function<T, ID> idExtractor;

    protected InMemoryRepository(Function<T, ID> idExtractor) {
        this.idExtractor = idExtractor;
    }

    @Override public List<T> findAll() { return new ArrayList<>(entities.values()); }
    @Override public Optional<T> findById(ID id) { return Optional.ofNullable(entities.get(id)); }
    @Override public T save(T entity) { entities.put(idExtractor.apply(entity), entity); return entity; }
    @Override public boolean deleteById(ID id) { return entities.remove(id) != null; }
    @Override public void update(T entity) { entities.put(idExtractor.apply(entity), entity); }
    @Override public List<T> findBy(Predicate<T> predicate) {
        return entities.values().stream().filter(predicate).toList();
    }
}
