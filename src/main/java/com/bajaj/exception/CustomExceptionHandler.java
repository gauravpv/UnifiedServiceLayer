package com.bajaj.exception;

import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

public final class CustomExceptionHandler {

	public static ResponseHandler handle(Exception ex) {
		if (ex instanceof WebClientResponseException || ex instanceof WebClientRequestException) {
			return WebClientExceptionUtil.handle(ex);
		}else {
			 return ExceptionUtils.handleOtherException(ex);
		}
	}
}
