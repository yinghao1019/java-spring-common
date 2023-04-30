package com.example.common.exception;

public class NoMatchingResourceException extends RuntimeException {
    private static final long serialVersionUID = 7268077302571961961L;

    public NoMatchingResourceException() {
        super();
    }

    public NoMatchingResourceException(String message) {
        super(message);
    }

    public NoMatchingResourceException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoMatchingResourceException(Throwable cause) {
        super(cause);
    }

    public NoMatchingResourceException(
        String message,
        Throwable cause,
        boolean enableSuppression,
        boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
