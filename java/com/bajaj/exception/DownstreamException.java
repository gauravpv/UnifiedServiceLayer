package com.bajaj.exception;

import lombok.Getter;

@Getter
public class DownstreamException extends RuntimeException {
    private final String errorCode;
    private final int statusCode;

    public DownstreamException(String errorCode, String message, int statusCode) {
        super(message);
        this.errorCode = errorCode;
        this.statusCode = statusCode;
    }

    public DownstreamException(String errorCode, String message, int statusCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.statusCode = statusCode;
    }
}
