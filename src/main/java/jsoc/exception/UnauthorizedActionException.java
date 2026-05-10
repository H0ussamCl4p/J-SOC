package jsoc.exception;

public class UnauthorizedActionException extends JSocException {

    public UnauthorizedActionException(String username, String action) {
        super("User '" + username + "' is not authorized to perform action: '" + action + "'.");
    }
}
