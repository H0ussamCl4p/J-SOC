package jsoc.service;

import jsoc.model.incident.Incident;
import jsoc.model.enums.IncidentStatus;
import jsoc.repository.IncidentRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * SLAService — suivi des délais SLA (Service Level Agreement).
 *
 * Le délai SLA est calculé via incident.computeDefaultSLAHours()
 * (défini dans chaque sous-classe d'Incident).
 *
 * Valeurs typiques par sévérité :
 *   CRITICAL  →  4 heures
 *   HIGH      →  8 heures
 *   MEDIUM    → 24 heures
 *   LOW       → 72 heures
 *
 * Un SLA est "breached" si l'incident n'est pas RESOLVED/CLOSED
 * et que le délai est dépassé.
 */
public class SLAService {

    private final IncidentRepository incidentRepository;

    public SLAService(IncidentRepository incidentRepository) {
        this.incidentRepository = incidentRepository;
    }

    // ── Délai SLA ─────────────────────────────────────────────────────────────

    /**
     * Retourne la date limite SLA d'un incident.
     * Utilise computeDefaultSLAHours() défini dans la sous-classe concrète.
     */
    public LocalDateTime getDeadline(Incident incident) {
        int hours = incident.computeDefaultSLAHours();
        return incident.getCreatedAt().plusHours(hours);
    }

    // ── Détection de dépassement ──────────────────────────────────────────────

    /**
     * Retourne true si l'incident a dépassé son SLA.
     * Pour les incidents RESOLVED/CLOSED, vérifie si la résolution
     * a été enregistrée avant la deadline (basé sur l'historique).
     * Pour les incidents actifs, compare avec maintenant.
     */
    public boolean isSlaBreached(Incident incident) {
        LocalDateTime deadline = getDeadline(incident);
        IncidentStatus status  = incident.getStatus();

        if (status == IncidentStatus.RESOLVED || status == IncidentStatus.CLOSED) {
            // Cherche la date de résolution dans l'historique
            LocalDateTime resolvedAt = incident.getHistory().stream()
                    .filter(h -> "STATUS_CHANGE".equals(h.getAction())
                              && IncidentStatus.RESOLVED.name().equals(h.getNewValue()))
                    .map(h -> h.getTimestamp())
                    .findFirst()
                    .orElse(null);
            return resolvedAt != null && resolvedAt.isAfter(deadline);
        }

        return LocalDateTime.now().isAfter(deadline);
    }

    /**
     * Retourne true si la deadline approche dans moins de 2 heures
     * mais n'est pas encore dépassée. Utile pour afficher des avertissements.
     */
    public boolean isSlaWarning(Incident incident) {
        if (isSlaBreached(incident)) return false;
        IncidentStatus s = incident.getStatus();
        if (s == IncidentStatus.RESOLVED || s == IncidentStatus.CLOSED) return false;

        LocalDateTime deadline      = getDeadline(incident);
        LocalDateTime warnThreshold = LocalDateTime.now().plusHours(2);
        return warnThreshold.isAfter(deadline);
    }

    // ── Temps restant / écoulé ────────────────────────────────────────────────

    /**
     * Retourne une chaîne lisible indiquant le temps restant avant dépassement.
     * Ex : "3h 20m restant" ou "DÉPASSÉ (depuis 45m)"
     */
    public String getTimeRemaining(Incident incident) {
        LocalDateTime deadline = getDeadline(incident);
        LocalDateTime now      = LocalDateTime.now();

        if (now.isAfter(deadline)) {
            Duration overdue = Duration.between(deadline, now);
            return "DÉPASSÉ (depuis " + formatDuration(overdue) + ")";
        }

        Duration remaining = Duration.between(now, deadline);
        return formatDuration(remaining) + " restant";
    }

    /**
     * Retourne la durée de résolution d'un incident résolu,
     * calculée depuis l'historique. Retourne null si non résolu.
     */
    public Duration getResolutionTime(Incident incident) {
        return incident.getHistory().stream()
                .filter(h -> "STATUS_CHANGE".equals(h.getAction())
                          && IncidentStatus.RESOLVED.name().equals(h.getNewValue()))
                .map(h -> Duration.between(incident.getCreatedAt(), h.getTimestamp()))
                .findFirst()
                .orElse(null);
    }

    // ── Requêtes groupées ─────────────────────────────────────────────────────

    /**
     * Retourne tous les incidents actifs qui ont dépassé leur SLA.
     */
    public List<Incident> getBreachedIncidents() {
        return incidentRepository.findAll().stream()
                .filter(i -> i.getStatus() != IncidentStatus.CLOSED)
                .filter(this::isSlaBreached)
                .collect(Collectors.toList());
    }

    /**
     * Retourne tous les incidents dont la deadline approche (< 2h).
     */
    public List<Incident> getWarningIncidents() {
        return incidentRepository.findAll().stream()
                .filter(i -> i.getStatus() != IncidentStatus.RESOLVED
                          && i.getStatus() != IncidentStatus.CLOSED)
                .filter(this::isSlaWarning)
                .collect(Collectors.toList());
    }

    /**
     * Affiche un rapport SLA formaté dans la console.
     */
    public void printSlaReport() {
        List<Incident> active = incidentRepository.findAll().stream()
                .filter(i -> i.getStatus() != IncidentStatus.CLOSED)
                .collect(Collectors.toList());

        System.out.println("╔══════════════════════════════════════════════════════╗");
        System.out.println("║               RAPPORT SLA — INCIDENTS ACTIFS         ║");
        System.out.println("╠══════════════════════════════════════════════════════╣");
        System.out.printf( "║  %-10s %-10s %-12s %-16s ║%n",
                "ID", "SÉVÉRITÉ", "STATUT", "SLA");
        System.out.println("╠══════════════════════════════════════════════════════╣");

        for (Incident i : active) {
            String slaLabel = isSlaBreached(i) ? "⛔ DÉPASSÉ"
                            : isSlaWarning(i)  ? "⚠  AVERTISSEMENT"
                                               : "✅ OK";
            System.out.printf("║  %-10s %-10s %-12s %-16s ║%n",
                    truncate(i.getId(), 10),
                    i.getSeverity(),
                    i.getStatus(),
                    slaLabel);
        }

        System.out.println("╠══════════════════════════════════════════════════════╣");
        System.out.printf( "║  Total : %-5d  Dépassés : %-5d  Avertissements : %-3d ║%n",
                active.size(),
                getBreachedIncidents().size(),
                getWarningIncidents().size());
        System.out.println("╚══════════════════════════════════════════════════════╝");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String formatDuration(Duration d) {
        long hours   = d.toHours();
        long minutes = d.toMinutesPart();
        if (hours > 0) return hours + "h " + minutes + "m";
        return minutes + "m";
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }
}
