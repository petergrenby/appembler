package se.grenby.appembler.exception;

public class NoMatchingConstructorException extends RuntimeException {

    public NoMatchingConstructorException(String message) {
        super(message);
    }

}
