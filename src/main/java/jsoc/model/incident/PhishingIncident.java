package jsoc.model.incident;

import jsoc.model.enums.IncidentStatus;
import jsoc.model.enums.Severity;
import jsoc.model.user.User;

import java.time.LocalDateTime;

public class PhishingIncident extends Incident {

    public PhishingIncident(String title, String description, Severity severity) {
        super(title, description, severity);
    }

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
