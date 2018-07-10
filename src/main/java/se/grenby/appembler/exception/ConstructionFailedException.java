package se.grenby.appembler.exception;

public class ConstructionFailedException extends RuntimeException {

    public ConstructionFailedException(String message, Throwable cause) {
        super(message, cause);
    }

}
