package at.fhtw.mtcgapp.exception;

public class ConstraintViolationException extends RuntimeException{
    public ConstraintViolationException(String message) {
        super(message);
    }

    public ConstraintViolationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConstraintViolationException(Throwable cause) {
        super(cause);
    }
}
