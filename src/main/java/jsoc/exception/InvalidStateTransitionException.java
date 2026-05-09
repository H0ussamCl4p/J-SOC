package jsoc.exception;

public class InvalidStateTransitionException extends JSocException {

    public InvalidStateTransitionException(String fromState, String toState) {
        super("Invalid state transition: cannot move from '" + fromState + "' to '" + toState + "'.");
    }

    public InvalidStateTransitionException(String fromState, String toState, String reason) {
        super("Invalid state transition from '" + fromState + "' to '" + toState + "': " + reason);
    }
}
