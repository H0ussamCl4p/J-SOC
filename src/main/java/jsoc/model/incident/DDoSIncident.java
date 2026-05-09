package jsoc.model.incident;

import jsoc.model.enums.IncidentStatus;
import jsoc.model.enums.Severity;
import jsoc.model.user.User;

import java.time.LocalDateTime;

/**
 * Incident de type <b>DDoS</b> (Distributed Denial of Service).
 *
 * <p>Caractéristiques :</p>
 * <ul>
 *   <li><b>CWE-400</b> — Uncontrolled Resource Consumption</li>
 *   <li><b>SLA par défaut</b> : 2 heures</li>
 *   <li><b>Procédure</b> : mitigation réseau (filtrage, blackholing) + analyse du trafic</li>
 * </ul>
 */
public class DDoSIncident extends Incident {

    public DDoSIncident(String title, String description, Severity severity) {
        super(title, description, severity);
    }

    /** Constructeur complet pour la restauration CSV. */
    public DDoSIncident(String id, String title, String description,
                        Severity severity, IncidentStatus status,
                        LocalDateTime createdAt, User assignedTo) {
        super(id, title, description, severity, status, createdAt, assignedTo);
    }

    @Override
    public String getType() {
        return "DDOS";
    }

    @Override
    public String getResponseProcedure() {
        return "Mitigation réseau + analyse trafic";
    }

    @Override
    public int computeDefaultSLAHours() {
        return 2;
    }

    @Override
    public String getCWE() {
        return "CWE-400";
    }
}
