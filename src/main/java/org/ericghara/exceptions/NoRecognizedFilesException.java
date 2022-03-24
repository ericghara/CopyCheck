package org.ericghara.exceptions;

public class NoRecognizedFilesException extends RuntimeException {

    public NoRecognizedFilesException() {
        super();
    }

    public NoRecognizedFilesException(String message) {
        super(message);
    }

    public NoRecognizedFilesException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoRecognizedFilesException(Throwable cause) {
        super(cause);
    }
}
