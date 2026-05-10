package jsoc.service;

import jsoc.exception.IncidentNotFoundException;
import jsoc.exception.InvalidStateTransitionException;
import jsoc.exception.UnauthorizedActionException;
import jsoc.model.HistoryEntry;
import jsoc.model.incident.Incident;
import jsoc.model.enums.IncidentStatus;
import jsoc.model.user.Analyst;
import jsoc.model.user.User;
import jsoc.repository.IncidentRepository;
import jsoc.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * AssignmentService — gestion de l'affectation des analystes aux incidents.
 *
 * Règles métier :
 *  - Seul un Manager peut assigner / réassigner / désassigner.
 *  - Seuls les Analysts peuvent être assignés (pas les Managers).
 *  - Un incident doit être NEW ou ESCALATED pour être assigné.
 *  - L'assignation d'un incident NEW le fait passer en IN_PROGRESS.
 */
public class AssignmentService {

    private final IncidentRepository incidentRepository;
    private final UserRepository     userRepository;
    private final AuthService        authService;

    public AssignmentService(IncidentRepository incidentRepository,
                             UserRepository     userRepository,
                             AuthService        authService) {
        this.incidentRepository = incidentRepository;
        this.userRepository     = userRepository;
        this.authService        = authService;
    }

    // ── Assigner ──────────────────────────────────────────────────────────────

    /**
     * Assigne un analyste à un incident.
     * Manager uniquement. L'incident doit être NEW ou ESCALATED.
     */
    public void assign(String incidentId, String analystUsername)
            throws IncidentNotFoundException, UnauthorizedActionException,
                   InvalidStateTransitionException {

        authService.requirePermission(User.ACTION_ASSIGN);

        Incident incident = findIncident(incidentId);
        Analyst  analyst  = findAnalyst(analystUsername);

        validateAssignable(incident);

        // Transition NEW → IN_PROGRESS automatique à la première assignation
        if (incident.getStatus() == IncidentStatus.NEW) {
            try {
                incident.transitionTo(IncidentStatus.IN_PROGRESS);
            } catch (IllegalStateException e) {
                throw new InvalidStateTransitionException(
                    "NEW -> IN_PROGRESS", e.getMessage());
            }
        }

        incident.assignTo(analyst);

        incident.addHistoryEntry(new HistoryEntry(
            "ASSIGNED",
            authService.getCurrentUser().getUsername(),
            null,
            analyst.getUsername()
        ));

        incidentRepository.save(incident);
        System.out.println("[ASSIGNMENT] " + incidentId + " → " + analystUsername);
    }

    // ── Réassigner ────────────────────────────────────────────────────────────

    /**
     * Réassigne un incident d'un analyste à un autre.
     * Manager uniquement.
     */
    public void reassign(String incidentId, String newAnalystUsername)
            throws IncidentNotFoundException, UnauthorizedActionException,
                   InvalidStateTransitionException {

        authService.requirePermission(User.ACTION_ASSIGN);

        Incident incident = findIncident(incidentId);
        Analyst  analyst  = findAnalyst(newAnalystUsername);

        if (!incident.isAssigned()) {
            throw new IllegalStateException(
                "L'incident " + incidentId + " n'est pas encore assigné.");
        }

        String previousAssignee = incident.getAssignee().getUsername();
        incident.assignTo(analyst);

        incident.addHistoryEntry(new HistoryEntry(
            "REASSIGNED",
            authService.getCurrentUser().getUsername(),
            previousAssignee,
            analyst.getUsername()
        ));

        incidentRepository.save(incident);
        System.out.println("[ASSIGNMENT] " + incidentId
                + " réassigné : " + previousAssignee + " → " + newAnalystUsername);
    }

    // ── Désassigner ───────────────────────────────────────────────────────────

    /**
     * Retire l'assignation d'un incident. Manager uniquement.
     * L'incident repasse en NEW si il était IN_PROGRESS.
     */
    public void unassign(String incidentId)
            throws IncidentNotFoundException, UnauthorizedActionException {

        authService.requirePermission(User.ACTION_ASSIGN);

        Incident incident = findIncident(incidentId);

        if (!incident.isAssigned()) {
            throw new IllegalStateException(
                "L'incident " + incidentId + " n'est pas assigné.");
        }

        String previous = incident.getAssignee().getUsername();
        incident.unassign();

        incident.addHistoryEntry(new HistoryEntry(
            "UNASSIGNED",
            authService.getCurrentUser().getUsername(),
            previous,
            null
        ));

        incidentRepository.save(incident);
        System.out.println("[ASSIGNMENT] " + incidentId + " désassigné.");
    }

    // ── Requêtes ──────────────────────────────────────────────────────────────

    /**
     * Retourne tous les incidents assignés à un analyste donné.
     */
    public List<Incident> getIncidentsByAnalyst(String analystUsername) {
        return incidentRepository.findAll().stream()
                .filter(i -> i.isAssigned()
                          && i.getAssignee().getUsername().equals(analystUsername))
                .collect(Collectors.toList());
    }

    /**
     * Retourne tous les incidents non assignés (statut NEW).
     */
    public List<Incident> getUnassignedIncidents() {
        return incidentRepository.findAll().stream()
                .filter(i -> !i.isAssigned()
                          && i.getStatus() == IncidentStatus.NEW)
                .collect(Collectors.toList());
    }

    /**
     * Retourne un résumé de la charge de travail de chaque analyste.
     * Format : "username — N incident(s) actif(s)"
     */
    public List<String> getWorkloadSummary() {
        return userRepository.findAll().stream()
                .filter(u -> u instanceof Analyst)
                .map(analyst -> {
                    long count = incidentRepository.findAll().stream()
                            .filter(i -> i.isAssigned()
                                      && i.getAssignee().getUsername()
                                          .equals(analyst.getUsername())
                                      && i.getStatus() != IncidentStatus.RESOLVED
                                      && i.getStatus() != IncidentStatus.CLOSED)
                            .count();
                    return analyst.getUsername() + " — " + count + " incident(s) actif(s)";
                })
                .collect(Collectors.toList());
    }

    // ── Helpers privés ────────────────────────────────────────────────────────

    private Incident findIncident(String id) throws IncidentNotFoundException {
        Optional<Incident> opt = incidentRepository.findById(id);
        if (opt.isEmpty()) {
            throw new IncidentNotFoundException("Incident introuvable : " + id);
        }
        return opt.get();
    }

    private Analyst findAnalyst(String username) {
        Optional<User> opt = userRepository.findByUsername(username);
        if (opt.isEmpty()) {
            throw new IllegalArgumentException("Utilisateur introuvable : " + username);
        }
        User user = opt.get();
        if (!(user instanceof Analyst)) {
            throw new IllegalArgumentException(
                "L'utilisateur '" + username + "' n'est pas un Analyst.");
        }
        return (Analyst) user;
    }

    private void validateAssignable(Incident incident)
            throws InvalidStateTransitionException {
        IncidentStatus s = incident.getStatus();
        if (s != IncidentStatus.NEW && s != IncidentStatus.ESCALATED) {
            throw new InvalidStateTransitionException(
                s.name() + " -> ASSIGNED",
                "Impossible d'assigner l'incident " + incident.getId()
                + " avec le statut " + s + ". Statut requis : NEW ou ESCALATED."
            );
        }
    }
}
