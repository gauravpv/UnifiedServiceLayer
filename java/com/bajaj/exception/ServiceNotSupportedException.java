package com.bajaj.exception;

public class ServiceNotSupportedException extends RuntimeException {
    public ServiceNotSupportedException(String message) {
        super(message);
    }
}
