package jsoc.model.enums;

public enum IncidentStatus {

    NEW,
    ASSIGNED,
    IN_PROGRESS,
    RESOLVED,
    CLOSED,
    ESCALATED;

    public boolean canTransitionTo(IncidentStatus next) {
        if (next == null) {
            return false;
        }
        return switch (this) {
            case NEW         -> next == ASSIGNED;
            case ASSIGNED    -> next == IN_PROGRESS;
            case IN_PROGRESS -> next == RESOLVED || next == ESCALATED;
            case RESOLVED    -> next == CLOSED;
            case ESCALATED   -> next == IN_PROGRESS || next == RESOLVED;
            case CLOSED      -> false;
        };
    }

    public boolean isTerminal() {
        return this == CLOSED;
    }
}
