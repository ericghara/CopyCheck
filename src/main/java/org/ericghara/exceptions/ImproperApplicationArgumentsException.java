package org.ericghara.exceptions;

public class ImproperApplicationArgumentsException extends RuntimeException{
    public ImproperApplicationArgumentsException() {
    }

    public ImproperApplicationArgumentsException(String message) {
        super(message);
    }

    public ImproperApplicationArgumentsException(String message, Throwable cause) {
        super(message, cause);
    }

    public ImproperApplicationArgumentsException(Throwable cause) {
        super(cause);
    }
}
