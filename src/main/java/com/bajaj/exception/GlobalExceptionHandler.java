package com.bajaj.exception;

import com.bajaj.dto.EncryptedResponseEnvelope;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({MethodArgumentNotValidException.class, BadRequestException.class,
            CryptoException.class, HttpMessageNotReadableException.class,
            ServiceNotSupportedException.class})
    public ResponseEntity<EncryptedResponseEnvelope> handleBadRequest(Exception ex) {
        String msg = (ex instanceof MethodArgumentNotValidException mex)
                ? mex.getBindingResult().getFieldErrors().stream()
                    .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                    .collect(Collectors.joining(", "))
                : ex.getMessage();
        log.warn("400: {}", msg);
        return envelope(HttpStatus.BAD_REQUEST, msg);
    }

    @ExceptionHandler(DownstreamException.class)
    public ResponseEntity<EncryptedResponseEnvelope> handleDownstream(DownstreamException ex) {
        HttpStatus status = switch (ex.getStatusCode()) {
            case 504 -> HttpStatus.GATEWAY_TIMEOUT;
            case 503 -> HttpStatus.SERVICE_UNAVAILABLE;
            default -> HttpStatus.BAD_GATEWAY;
        };
        log.error("Downstream: {} - {}", ex.getErrorCode(), ex.getMessage());
        return envelope(status, ex.getErrorCode() + ": " + ex.getMessage());
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<EncryptedResponseEnvelope> handleDb(DataAccessException ex) {
        log.error("DB error", ex);
        return envelope(HttpStatus.INTERNAL_SERVER_ERROR, "Database operation failed");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<EncryptedResponseEnvelope> handleGeneric(Exception ex) {
        log.error("Unhandled", ex);
        return envelope(HttpStatus.INTERNAL_SERVER_ERROR,
                ex.getMessage() == null ? "Internal server error" : ex.getMessage());
    }

    private static ResponseEntity<EncryptedResponseEnvelope> envelope(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(EncryptedResponseEnvelope.builder()
                .statusCode(String.valueOf(status.value()))
                .message(message)
                .build());
    }
}
