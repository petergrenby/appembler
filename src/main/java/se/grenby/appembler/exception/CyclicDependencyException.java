package se.grenby.appembler.exception;

public class CyclicDependencyException extends RuntimeException {

    public CyclicDependencyException(String message) {
        super(message);
    }

}
