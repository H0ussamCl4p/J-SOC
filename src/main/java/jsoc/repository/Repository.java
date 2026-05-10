package jsoc.repository;

import java.util.List;
import java.util.Optional;

/**
 * Generic CRUD repository interface.
 *
 * @param <T>  the entity type (e.g. Incident, User)
 * @param <ID> the ID type (String for both)
 */
public interface Repository<T, ID> {

    /** Save or update an entity. */
    void save(T entity);

    /** Find entity by its unique ID. */
    Optional<T> findById(ID id);

    /** Return all stored entities. */
    List<T> findAll();

    /** Delete entity by ID. */
    void deleteById(ID id);

    /** Check if an entity with this ID exists. */
    boolean existsById(ID id);

    /** Total number of stored entities. */
    int count();
}
