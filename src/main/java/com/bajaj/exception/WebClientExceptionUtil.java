package com.bajaj.exception;

import java.net.ConnectException;
import java.net.SocketTimeoutException;

import javax.net.ssl.SSLException;

import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class WebClientExceptionUtil {

	private WebClientExceptionUtil() {
	}

	
	public static ResponseHandler handle(Throwable e) {
		if (e == null) {
			return new ResponseHandler(500, "Failed", "Unknown WebClient error (null exception)", null);
		}

		var statusMessage = switch (e) {
		case WebClientResponseException ex -> handleResponseException(ex);
		case WebClientRequestException ex -> handleRequestException(ex);
		case SocketTimeoutException ignored -> new StatusMessage(504, "Request timed out while calling WebClient.");
		case ConnectException ignored -> new StatusMessage(503, "Unable to connect to WebClient endpoint.");
		case SSLException ignored -> new StatusMessage(495, "SSL handshake failed during WebClient WebClient call.");
		default -> new StatusMessage(500, "Unexpected WebClient error: " + e.getMessage());
		};
		return new ResponseHandler(statusMessage.statusCode(), "Failed", statusMessage.message(), e.getMessage());
	}

	private static StatusMessage handleResponseException(WebClientResponseException ex) {
		int status = ex.getStatusCode().value();
		String body = ex.getResponseBodyAsString();

		String msg = switch (status) {
		case 400 -> "Bad Request:" + body;
		case 401 -> "Unauthorized:" + body;
		case 403 -> "Forbidden: " + body;
		case 404 -> "Not Found: " + body;
		case 500 -> "Internal Server Error " + body;
		default ->
			"WebClient HTTP %d: %s".formatted(status, (body != null && !body.isBlank()) ? body : ex.getMessage());
		};

		return new StatusMessage(status, msg);
	}

	private static StatusMessage handleRequestException(WebClientRequestException ex) {
		if (ex.getCause() instanceof ConnectException) {
			return new StatusMessage(503, "Cannot reach WebClient endpoint." + ex.getMessage());
		} else if (ex.getCause() instanceof SocketTimeoutException) {
			return new StatusMessage(504, "WebClient request timed out while connecting." + ex.getMessage());
		} else if (ex.getCause() instanceof SSLException) {
			return new StatusMessage(495, "SSL handshake failed during WebClient call." + ex.getMessage());
		} else {
			return new StatusMessage(503, "Network error: " + ex.getMessage());
		}
	}

	public static ResponseHandler success(String message, int statusCode) {
		return new ResponseHandler(statusCode, "Success", message, null);
	}

	private record StatusMessage(int statusCode, String message) {
	}
}
