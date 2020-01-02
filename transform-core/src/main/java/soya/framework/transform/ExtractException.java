package soya.framework.transform;

public class ExtractException extends RuntimeException {
    public ExtractException() {
    }

    public ExtractException(String message) {
        super(message);
    }

    public ExtractException(String message, Throwable cause) {
        super(message, cause);
    }

    public ExtractException(Throwable cause) {
        super(cause);
    }
}
