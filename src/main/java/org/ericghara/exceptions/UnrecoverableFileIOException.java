package org.ericghara.exceptions;

public class UnrecoverableFileIOException extends RuntimeException {

    public UnrecoverableFileIOException() {
    }

    public UnrecoverableFileIOException(String message) {
        super(message);
    }

    public UnrecoverableFileIOException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnrecoverableFileIOException(Throwable cause) {
        super(cause);
    }
}
