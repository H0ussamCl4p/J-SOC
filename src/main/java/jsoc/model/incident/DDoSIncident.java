package jsoc.model.incident;

import jsoc.model.enums.IncidentStatus;
import jsoc.model.enums.Severity;
import jsoc.model.user.User;

import java.time.LocalDateTime;

public class DDoSIncident extends Incident {

    public DDoSIncident(String title, String description, Severity severity) {
        super(title, description, severity);
    }

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
        return "Mitigation reseau + analyse trafic";
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
