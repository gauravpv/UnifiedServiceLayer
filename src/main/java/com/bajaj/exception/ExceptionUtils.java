package com.bajaj.exception;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

public final class ExceptionUtils {

	private ExceptionUtils() {
	}

	/**
	 * Handle "other" exceptions like JSON, XML, File, NullPointer, etc. Returns a
	 * ResponseHandler directly
	 */
	public static ResponseHandler handleOtherException(Exception ex) {
		if (ex == null) {
			return new ResponseHandler(500, "Failed", "Unknown error", "Exception is null");
		}

		var statusMessage = switch (ex) {
		//Gson Exceptions
		case JsonSyntaxException e -> new StatusMessage(400, "Invalid JSON format: " + ex.getMessage());
		case JsonProcessingException e -> new StatusMessage(400, "Invalid JSON format: " + ex.getMessage());
		case JsonIOException e->new StatusMessage(500, "Gson I/O error: " + ex.getMessage());
		
		case IOException e -> new StatusMessage(500, "I/O exception occurred: " + e.getMessage());
		case NullPointerException e -> new StatusMessage(500, "Null pointer exception: " + e.getMessage());
		case IllegalArgumentException e -> new StatusMessage(400, "Illegal argument: " + e.getMessage());
		case ArrayIndexOutOfBoundsException e -> new StatusMessage(400, "Index out of bounds: " + e.getMessage());
		case IndexOutOfBoundsException e -> new StatusMessage(400, "Index out of bounds: " + e.getMessage());
		case ArithmeticException e -> new StatusMessage(400, "Arithmetic error: " + e.getMessage());
		default -> new StatusMessage(500, "Unexpected exception: " + ex.getMessage());
		};

		return new ResponseHandler(statusMessage.statusCode(), "Failed", statusMessage.message(), ex.getMessage());
	}

	public static ResponseHandler success(String message, int statusCode) {
		return new ResponseHandler(statusCode, "Success", message, null);
	}

	private record StatusMessage(int statusCode, String message) {
	}
}
