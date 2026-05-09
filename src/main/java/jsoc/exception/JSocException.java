package jsoc.exception;

public class JSocException extends RuntimeException {

    public JSocException(String message) {
        super(message);
    }

    public JSocException(String message, Throwable cause) {
        super(message, cause);
    }
}
