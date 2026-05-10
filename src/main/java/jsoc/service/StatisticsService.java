package jsoc.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import jsoc.model.enums.IncidentStatus;
import jsoc.model.enums.Severity;
import jsoc.model.incident.Incident;
import jsoc.model.user.Analyst;
import jsoc.repository.IncidentRepository;
import jsoc.repository.UserRepository;

/**
 * StatisticsService — métriques et rapports SOC.
 *
 * Couvre :
 *  - Comptages par statut et sévérité
 *  - Temps de résolution moyen
 *  - Performance par analyste
 *  - Taux de dépassement SLA
 *  - Tendances (7j / 30j)
 */
public class StatisticsService {

    private final IncidentRepository incidentRepository;
    private final UserRepository     userRepository;
    private final SLAService         slaService;

    public StatisticsService(IncidentRepository incidentRepository,
                             UserRepository     userRepository,
                             SLAService         slaService) {
        this.incidentRepository = incidentRepository;
        this.userRepository     = userRepository;
        this.slaService         = slaService;
    }

    //  Comptages 

    public long getTotalIncidents() {
        return incidentRepository.findAll().size();
    }

    public Map<IncidentStatus, Long> countByStatus() {
        return incidentRepository.findAll().stream()
                .collect(Collectors.groupingBy(Incident::getStatus, Collectors.counting()));
    }

    public Map<Severity, Long> countBySeverity() {
        return incidentRepository.findAll().stream()
                .collect(Collectors.groupingBy(Incident::getSeverity, Collectors.counting()));
    }

    public long getOpenCount() {
        return incidentRepository.findAll().stream()
                .filter(i -> i.getStatus() != IncidentStatus.RESOLVED
                          && i.getStatus() != IncidentStatus.CLOSED)
                .count();
    }

    public long getResolvedCount() {
        return incidentRepository.findAll().stream()
                .filter(i -> i.getStatus() == IncidentStatus.RESOLVED
                          || i.getStatus() == IncidentStatus.CLOSED)
                .count();
    }

    // Temps de résolution

    /**
     * Retourne le temps de résolution moyen sur tous les incidents résolus.
     * Retourne null si aucun incident n'est résolu.
     */
    public Duration getAverageResolutionTime() {
        List<Duration> times = incidentRepository.findAll().stream()
                .map(slaService::getResolutionTime)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (times.isEmpty()) return null;

        long totalSeconds = times.stream().mapToLong(Duration::getSeconds).sum();
        return Duration.ofSeconds(totalSeconds / times.size());
    }

    /**
     * Retourne le temps de résolution moyen par sévérité.
     */
    public Map<Severity, String> getAverageResolutionTimeBySeverity() {
        Map<Severity, String> result = new LinkedHashMap<>();

        for (Severity severity : Severity.values()) {
            List<Duration> times = incidentRepository.findAll().stream()
                    .filter(i -> i.getSeverity() == severity)
                    .map(slaService::getResolutionTime)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            if (times.isEmpty()) {
                result.put(severity, "N/A");
            } else {
                long avgSecs = times.stream().mapToLong(Duration::getSeconds).sum()
                               / times.size();
                result.put(severity, formatDuration(Duration.ofSeconds(avgSecs)));
            }
        }

        return result;
    }

    //  SLA 

    public long getTotalSlaBreaches() {
        return incidentRepository.findAll().stream()
                .filter(slaService::isSlaBreached)
                .count();
    }

    public String getSlaBreachRate() {
        long total    = getTotalIncidents();
        long breached = getTotalSlaBreaches();
        if (total == 0) return "0.0%";
        return String.format("%.1f%%", (double) breached / total * 100.0);
    }

    //  Performance par analyste 

    /**
     * Retourne pour chaque analyste :
     *   username → { "resolved", "active", "avgResolutionTime" }
     */
    public Map<String, Map<String, String>> getAnalystPerformance() {
        Map<String, Map<String, String>> result = new LinkedHashMap<>();

        userRepository.findAll().stream()
                .filter(u -> u instanceof Analyst)
                .forEach(analyst -> {
                    String username = analyst.getUsername();

                    List<Incident> assigned = incidentRepository.findAll().stream()
                            .filter(i -> i.isAssigned()
                                      && i.getAssignee().getUsername().equals(username))
                            .collect(Collectors.toList());

                    long resolved = assigned.stream()
                            .filter(i -> i.getStatus() == IncidentStatus.RESOLVED
                                      || i.getStatus() == IncidentStatus.CLOSED)
                            .count();

                    long active = assigned.stream()
                            .filter(i -> i.getStatus() != IncidentStatus.RESOLVED
                                      && i.getStatus() != IncidentStatus.CLOSED)
                            .count();

                    List<Duration> times = assigned.stream()
                            .map(slaService::getResolutionTime)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());

                    String avgTime = times.isEmpty() ? "N/A"
                            : formatDuration(Duration.ofSeconds(
                                times.stream().mapToLong(Duration::getSeconds).sum()
                                / times.size()));

                    Map<String, String> stats = new LinkedHashMap<>();
                    stats.put("resolved",          String.valueOf(resolved));
                    stats.put("active",            String.valueOf(active));
                    stats.put("avgResolutionTime", avgTime);
                    result.put(username, stats);
                });

        return result;
    }

    //  Tendances

    public long getIncidentsCreatedInLastDays(int days) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(days);
        return incidentRepository.findAll().stream()
                .filter(i -> i.getCreatedAt() != null
                          && i.getCreatedAt().isAfter(cutoff))
                .count();
    }

    public long getIncidentsResolvedInLastDays(int days) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(days);
        return incidentRepository.findAll().stream()
                .filter(i -> i.getHistory().stream()
                        .anyMatch(h -> "STATUS_CHANGE".equals(h.getAction())
                                && "RESOLVED".equals(h.getNewValue())
                                && h.getTimestamp().isAfter(cutoff)))
                .count();
    }

    // Dashboard complet 

    public void printDashboard() {
        Duration avgRes = getAverageResolutionTime();
        Map<IncidentStatus, Long> byStatus   = countByStatus();
        Map<Severity, Long>       bySeverity = countBySeverity();

        System.out.println("╔══════════════════════════════════════════════════════╗");
        System.out.println("║           J-SOC — TABLEAU DE BORD STATISTIQUES       ║");
        System.out.println("╠══════════════════════════════════════════════════════╣");

        System.out.println("║  VUE GÉNÉRALE                                        ║");
        System.out.printf( "║  Total incidents       : %-27d ║%n", getTotalIncidents());
        System.out.printf( "║  Ouverts / Actifs      : %-27d ║%n", getOpenCount());
        System.out.printf( "║  Résolus / Clôturés    : %-27d ║%n", getResolvedCount());
        System.out.printf( "║  Dépassements SLA      : %-20d (%6s) ║%n",
                getTotalSlaBreaches(), getSlaBreachRate());
        System.out.printf( "║  Rés. moyen            : %-27s ║%n",
                avgRes == null ? "N/A" : formatDuration(avgRes));

        System.out.println("╠══════════════════════════════════════════════════════╣");
        System.out.println("║  PAR STATUT                                          ║");
        for (IncidentStatus s : IncidentStatus.values()) {
            System.out.printf("║  %-18s : %-30d ║%n", s, byStatus.getOrDefault(s, 0L));
        }

        System.out.println("╠══════════════════════════════════════════════════════╣");
        System.out.println("║  PAR SÉVÉRITÉ                                        ║");
        for (Severity sv : Severity.values()) {
            System.out.printf("║  %-10s : %-38d ║%n", sv, bySeverity.getOrDefault(sv, 0L));
        }

        System.out.println("╠══════════════════════════════════════════════════════╣");
        System.out.println("║  TENDANCES                                           ║");
        System.out.printf( "║  Créés   (7j)  : %-35d ║%n", getIncidentsCreatedInLastDays(7));
        System.out.printf( "║  Créés   (30j) : %-35d ║%n", getIncidentsCreatedInLastDays(30));

        System.out.println("╠══════════════════════════════════════════════════════╣");
        System.out.println("║  PERFORMANCE ANALYSTES                               ║");
        Map<String, Map<String, String>> perf = getAnalystPerformance();
        if (perf.isEmpty()) {
            System.out.println("║  Aucun analyste enregistré.                          ║");
        } else {
            System.out.printf("║  %-15s %8s %8s %15s ║%n",
                    "Analyste", "Résolus", "Actifs", "Moy. résol.");
            System.out.println("║  ──────────────────────────────────────────────────  ║");
            for (Map.Entry<String, Map<String, String>> e : perf.entrySet()) {
                Map<String, String> s = e.getValue();
                System.out.printf("║  %-15s %8s %8s %15s ║%n",
                        truncate(e.getKey(), 15),
                        s.get("resolved"),
                        s.get("active"),
                        s.get("avgResolutionTime"));
            }
        }

        System.out.println("╠══════════════════════════════════════════════════════╣");
        System.out.println("║  TEMPS MOYEN DE RÉSOLUTION PAR SÉVÉRITÉ              ║");
        getAverageResolutionTimeBySeverity().forEach((sv, t) ->
            System.out.printf("║  %-10s : %-38s ║%n", sv, t));

        System.out.println("╚══════════════════════════════════════════════════════╝");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String formatDuration(Duration d) {
        if (d == null) return "N/A";
        long hours   = d.toHours();
        long minutes = d.toMinutesPart();
        if (hours >= 24) {
            long days = d.toDays();
            return days + "j " + (hours % 24) + "h " + minutes + "m";
        }
        if (hours > 0) return hours + "h " + minutes + "m";
        return minutes + "m";
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }
}
