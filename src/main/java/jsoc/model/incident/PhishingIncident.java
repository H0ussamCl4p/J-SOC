package jsoc.model.incident;

import jsoc.model.enums.IncidentStatus;
import jsoc.model.enums.Severity;
import jsoc.model.user.User;

import java.time.LocalDateTime;

/**
 * Incident de type <b>phishing</b> (hameçonnage).
 *
 * <p>Caractéristiques :</p>
 * <ul>
 *   <li><b>CWE-1021</b> — Improper Restriction of Rendered UI Layers or Frames</li>
 *   <li><b>SLA par défaut</b> : 4 heures</li>
 *   <li><b>Procédure</b> : analyser l'email source, vérifier l'URL, exécuter dans une sandbox</li>
 * </ul>
 *
 * <p>Cette classe n'ajoute aucun état spécifique : elle redéfinit simplement les
 * 4 méthodes abstraites pour fournir les valeurs propres au phishing.</p>
 */
public class PhishingIncident extends Incident {

    public PhishingIncident(String title, String description, Severity severity) {
        super(title, description, severity);
    }

    /** Constructeur complet pour la restauration CSV. */
    public PhishingIncident(String id, String title, String description,
                            Severity severity, IncidentStatus status,
                            LocalDateTime createdAt, User assignedTo) {
        super(id, title, description, severity, status, createdAt, assignedTo);
    }

    @Override
    public String getType() {
        return "PHISHING";
    }

    @Override
    public String getResponseProcedure() {
        return "Analyse email + URL + sandbox";
    }

    @Override
    public int computeDefaultSLAHours() {
        return 4;
    }

    @Override
    public String getCWE() {
        return "CWE-1021";
    }
}
