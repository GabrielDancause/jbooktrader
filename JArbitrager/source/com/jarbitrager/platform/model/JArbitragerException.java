package com.jarbitrager.platform.model;

public class JArbitragerException extends Exception {
    public JArbitragerException(String message) {
        super(message);
    }

    public JArbitragerException(Throwable e) {
        super(e);
    }

    public JArbitragerException(String message, Throwable cause) {
        super(message, cause);
    }
}
