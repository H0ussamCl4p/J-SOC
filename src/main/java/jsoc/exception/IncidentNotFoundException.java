package jsoc.exception;

public class IncidentNotFoundException extends JSocException {

    public IncidentNotFoundException(String incidentId) {
        super("Incident not found with ID: '" + incidentId + "'.");
    }
}
