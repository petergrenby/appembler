package se.grenby.appembler.exception;

public class AmbiguousConstructorMatchingException extends RuntimeException {

    public AmbiguousConstructorMatchingException(String message) {
        super(message);
    }

}
