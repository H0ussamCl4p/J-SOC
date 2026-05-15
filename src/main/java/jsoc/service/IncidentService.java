package jsoc.service;

import jsoc.exception.IncidentNotFoundException;
import jsoc.exception.InvalidStateTransitionException;
import jsoc.exception.UnauthorizedActionException;
import jsoc.model.Comment;
import jsoc.model.HistoryEntry;
import jsoc.model.incident.Incident;
import jsoc.model.enums.IncidentStatus;
import jsoc.model.enums.Severity;
import jsoc.model.user.User;
import jsoc.repository.IncidentRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * IncidentService — gestion du cycle de vie complet des incidents.
 *
 * Machine à états (définie dans IncidentStatus.canTransitionTo) :
 *   NEW → IN_PROGRESS → RESOLVED → CLOSED
 *                     ↘ ESCALATED → IN_PROGRESS
 */
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final AuthService        authService;

    public IncidentService(IncidentRepository incidentRepository, AuthService authService) {
        this.incidentRepository = incidentRepository;
        this.authService        = authService;
    }

    // ── CRUD ──────────────────────────────────────────────────────────────────

    /**
     * Enregistre un incident déjà construit (l'ID est généré par Incident lui-même).
     */
    public Incident createIncident(Incident incident) throws UnauthorizedActionException {
        authService.requirePermission(User.ACTION_CREATE_INCIDENT);

        // Trace l'historique de création
        incident.addHistoryEntry(new HistoryEntry(
            "CREATION",
            authService.getCurrentUser().getUsername(),
            null,
            incident.getStatus().name()
        ));

        incidentRepository.save(incident);
        System.out.println("[INCIDENT] Créé : " + incident.getId());
        return incident;
    }

    /**
     * Retourne un incident par ID, ou lève IncidentNotFoundException.
     */
    public Incident getById(String id) throws IncidentNotFoundException {
        Optional<Incident> opt = incidentRepository.findById(id);
        if (opt.isEmpty()) {
            throw new IncidentNotFoundException("Incident introuvable : " + id);
        }
        return opt.get();
    }

    /**
     * Retourne tous les incidents.
     */
    public List<Incident> getAllIncidents() {
        return incidentRepository.findAll();
    }

    /**
     * Supprime un incident. Manager uniquement.
     */
    public void deleteIncident(String id)
            throws IncidentNotFoundException, UnauthorizedActionException {
        authService.requirePermission(User.ACTION_DELETE);
        getById(id); // vérifie l'existence
        incidentRepository.deleteById(id);
        System.out.println("[INCIDENT] Supprimé : " + id);
    }

    // ── Machine à états ───────────────────────────────────────────────────────

    /**
     * Effectue une transition de statut sur un incident.
     * Délègue la validation à IncidentStatus.canTransitionTo().
     */
    public void changeStatus(String incidentId, IncidentStatus newStatus)
            throws IncidentNotFoundException, InvalidStateTransitionException,
                   UnauthorizedActionException {

        authService.requirePermission(User.ACTION_UPDATE_STATUS);
        Incident incident = getById(incidentId);
        IncidentStatus oldStatus = incident.getStatus();

        // CLOSE nécessite le rôle Manager
        if (newStatus == IncidentStatus.CLOSED) {
            authService.requirePermission(User.ACTION_CLOSE);
        }

        // transitionTo() lève IllegalStateException si la transition est invalide
        try {
            incident.transitionTo(newStatus);
        } catch (IllegalStateException e) {
            throw new InvalidStateTransitionException(
                oldStatus.name() + " -> " + newStatus.name() + " : " + e.getMessage()
            );
        }

        incident.addHistoryEntry(new HistoryEntry(
            "STATUS_CHANGE",
            authService.getCurrentUser().getUsername(),
            oldStatus.name(),
            newStatus.name()
        ));

        incidentRepository.save(incident);
        System.out.println("[INCIDENT] " + incidentId
                + " : " + oldStatus + " → " + newStatus);
    }

    // ── Commentaires ──────────────────────────────────────────────────────────

    /**
     * Ajoute un commentaire à un incident.
     */
    public void addComment(String incidentId, String text)
            throws IncidentNotFoundException, UnauthorizedActionException {
        authService.requirePermission(User.ACTION_COMMENT);
        Incident incident = getById(incidentId);

        Comment comment = new Comment(
            authService.getCurrentUser().getUsername(),
            text
        );
        incident.addComment(comment);

        incident.addHistoryEntry(new HistoryEntry(
            "COMMENT_ADDED",
            authService.getCurrentUser().getUsername(),
            null,
            text
        ));

        incidentRepository.save(incident);
    }

    // ── Filtres ───────────────────────────────────────────────────────────────

    /** Retourne tous les incidents avec le statut donné. */
    public List<Incident> getByStatus(IncidentStatus status) {
        return incidentRepository.findAll().stream()
                .filter(i -> i.getStatus() == status)
                .collect(Collectors.toList());
    }

    /** Retourne tous les incidents avec la sévérité donnée. */
    public List<Incident> getBySeverity(Severity severity) {
        return incidentRepository.findAll().stream()
                .filter(i -> i.getSeverity() == severity)
                .collect(Collectors.toList());
    }

    /** Retourne tous les incidents assignés à un analyste donné. */
    public List<Incident> getByAssignee(String username) {
        return incidentRepository.findAll().stream()
                .filter(i -> i.isAssigned()
                          && i.getAssignee().getUsername().equals(username))
                .collect(Collectors.toList());
    }

    /** Retourne les incidents actifs (non clôturés). */
    public List<Incident> getActiveIncidents() {
        return incidentRepository.findAll().stream()
                .filter(i -> i.getStatus() != IncidentStatus.CLOSED)
                .collect(Collectors.toList());
    }

    /** Retourne les incidents non assignés. */
    public List<Incident> getUnassignedIncidents() {
        return incidentRepository.findAll().stream()
                .filter(i -> !i.isAssigned()
                          && i.getStatus() == IncidentStatus.NEW)
                .collect(Collectors.toList());
    }
}
