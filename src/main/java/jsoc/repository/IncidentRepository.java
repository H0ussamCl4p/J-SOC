package jsoc.repository;

import jsoc.model.enums.IncidentStatus;
import jsoc.model.enums.Severity;
import jsoc.model.incident.Incident;
import jsoc.model.user.User;
import jsoc.repository.csv.CsvHelper;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Repository for Incident entities with CSV persistence.
 *
 * NOTE: ID generation (INC-001, INC-002...) is handled by the Incident class
 * itself via its static counter. Member 3 creates incidents with the short
 * constructor: new PhishingIncident(title, description, severity).
 *
 * This repository takes a UserRepository so it can re-link the assignedTo
 * User object when loading incidents from the CSV file.
 */
public class IncidentRepository extends AbstractRepository<Incident, String> {

    private static final String CSV_FILE = "data/incidents.csv";

    private final UserRepository userRepository;

    public IncidentRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
        load();
    }

    // ---- Query methods for Member 3 (services) ----

    /** Find all incidents with a given status. */
    public List<Incident> findByStatus(IncidentStatus status) {
        return store.values().stream()
                .filter(i -> i.getStatus() == status)
                .collect(Collectors.toList());
    }

    /** Find all incidents with a given severity level. */
    public List<Incident> findBySeverity(Severity severity) {
        return store.values().stream()
                .filter(i -> i.getSeverity() == severity)
                .collect(Collectors.toList());
    }

    /** Find all incidents assigned to a specific user (by username). */
    public List<Incident> findByAssignee(String username) {
        return store.values().stream()
                .filter(i -> i.isAssigned() && i.getAssignee().getUsername().equals(username))
                .collect(Collectors.toList());
    }

    /** Find all incidents that are not yet CLOSED. */
    public List<Incident> findOpen() {
        return store.values().stream()
                .filter(i -> i.getStatus() != IncidentStatus.CLOSED)
                .collect(Collectors.toList());
    }

    // ---- Persistence ----

    @Override
    protected String getId(Incident incident) {
        return incident.getId();
    }

    @Override
    protected void load() {
        // Build a username -> User map so CsvHelper can resolve the assignedTo reference
        Map<String, User> userMap = userRepository.findAll().stream()
                .collect(Collectors.toMap(User::getUsername, u -> u));

        CsvHelper.loadIncidents(CSV_FILE, userMap)
                .forEach(i -> store.put(i.getId(), i));
    }

    @Override
    protected void persist() {
        CsvHelper.saveIncidents(CSV_FILE, findAll());
    }
}
