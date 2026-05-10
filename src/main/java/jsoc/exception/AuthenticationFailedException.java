package jsoc.exception;

public class AuthenticationFailedException extends JSocException {

    public AuthenticationFailedException(String username) {
        super("Authentication failed for user: '" + username + "'. Invalid credentials.");
    }
}
