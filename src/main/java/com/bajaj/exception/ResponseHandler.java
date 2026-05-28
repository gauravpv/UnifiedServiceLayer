package com.bajaj.exception;

public record ResponseHandler(int statusCode, String status, String message, String exception) {
	public boolean isSuccess() {
		return statusCode >= 200 && statusCode < 300;
	}
}