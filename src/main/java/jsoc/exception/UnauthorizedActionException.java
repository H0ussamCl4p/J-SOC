package jsoc.exception;

public class UnauthorizedActionException extends JSocException {

    public UnauthorizedActionException(String message) {
        super(message);
    }

    public UnauthorizedActionException(String action, String message) {
        super(message);
    }
}
