package jsoc.repository;

import java.util.*;

/**
 * Base implementation of Repository using an in-memory LinkedHashMap.
 * Subclasses handle CSV persistence and ID extraction.
 */
public abstract class AbstractRepository<T, ID> implements Repository<T, ID> {

    protected final Map<ID, T> store = new LinkedHashMap<>();

    /** Load data from the CSV file into the store. Called once at startup. */
    protected abstract void load();

    /** Persist the current state of the store to the CSV file. Called after every write. */
    protected abstract void persist();

    /** Extract the unique ID from an entity. */
    protected abstract ID getId(T entity);

    @Override
    public void save(T entity) {
        store.put(getId(entity), entity);
        persist();
    }

    @Override
    public Optional<T> findById(ID id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<T> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public void deleteById(ID id) {
        store.remove(id);
        persist();
    }

    @Override
    public boolean existsById(ID id) {
        return store.containsKey(id);
    }

    @Override
    public int count() {
        return store.size();
    }
}
