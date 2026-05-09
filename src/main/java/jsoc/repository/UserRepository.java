package jsoc.repository;

import jsoc.model.user.Analyst;
import jsoc.model.user.Manager;
import jsoc.model.user.User;
import jsoc.repository.csv.CsvHelper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Repository for User entities with CSV persistence.
 * Pre-loads default accounts (alice, bob, carol) on first run.
 */
public class UserRepository extends AbstractRepository<User, String> {

    private static final String CSV_FILE = "data/users.csv";

    public UserRepository() {
        load();
        if (store.isEmpty()) {
            loadDefaultUsers();
        }
    }

    /** Find a user by their username. Used by AuthService for login. */
    public Optional<User> findByUsername(String username) {
        return Optional.ofNullable(store.get(username));
    }

    /** Return only Analyst users. Used by AssignmentService for auto-assignment. */
    public List<User> findAllAnalysts() {
        return store.values().stream()
                .filter(u -> u instanceof Analyst)
                .collect(Collectors.toList());
    }

    @Override
    protected String getId(User user) {
        return user.getUsername();
    }

    @Override
    protected void load() {
        CsvHelper.loadUsers(CSV_FILE).forEach(u -> store.put(u.getUsername(), u));
    }

    @Override
    protected void persist() {
        CsvHelper.saveUsers(CSV_FILE, findAll());
    }

    /**
     * Seed default accounts defined in the README.
     * Only called the very first time (when the CSV file is empty or missing).
     */
    private void loadDefaultUsers() {
        // Using (String username, String password, String email) constructor — matches Member 1 exactly
        save(new Analyst("alice", "password", "alice@jsoc.local"));
        save(new Analyst("bob",   "password", "bob@jsoc.local"));
        save(new Manager("carol", "password", "carol@jsoc.local"));
    }
}
