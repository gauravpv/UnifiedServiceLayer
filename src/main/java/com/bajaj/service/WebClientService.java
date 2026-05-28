package com.bajaj.service;

import static com.bajaj.constants.Constants.API_EXCEPTION;
import static com.bajaj.constants.Constants.API_URL;
import static com.bajaj.constants.Constants.AUTHORIZATION_HEADER;
import static com.bajaj.constants.Constants.AUTH_CLIENT_ID;
import static com.bajaj.constants.Constants.AUTH_CLIENT_SECRET;
import static com.bajaj.constants.Constants.AUTH_TOKEN_URL;
import static com.bajaj.constants.Constants.BEARER;
import static com.bajaj.constants.Constants.CLIENT_CREDENTIALS;
import static com.bajaj.constants.Constants.CLIENT_ID;
import static com.bajaj.constants.Constants.CLIENT_SECRET;
import static com.bajaj.constants.Constants.CODE_200;
import static com.bajaj.constants.Constants.CODE_500;
import static com.bajaj.constants.Constants.FAILED_API_CALL;
import static com.bajaj.constants.Constants.GRANT_TYPE;
import static com.bajaj.constants.Constants.OCP_KEY_HEADER;
import static com.bajaj.constants.Constants.OCP_SUB_KEY;
import static com.bajaj.constants.Constants.RESOURCE;
import static com.bajaj.constants.Constants.RESOURCE_URL;
import static com.bajaj.constants.Constants.SUCCESS_API_CALL;

import java.util.Arrays;

import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

import com.bajaj.dto.AzureTokenResponse;
import com.bajaj.dto.ProcessResponse;
import com.bajaj.exception.CustomExceptionHandler;
import com.google.gson.Gson;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Mahesh Shelke
 *
 * 
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class WebClientService {

	private final WebClient webClient;
	private final Environment env;
	private final Gson gson;

	private static AzureTokenResponse azureTokenResponse = null;

	public ProcessResponse getResponseFromAPI(String requestBody, String caseId) {
		final long startTime = System.currentTimeMillis();
		ProcessResponse apiResponseDTO = null;

		for (var attempt = 1; attempt <= 3; attempt++) {
			try {
				getToken();
				log.info("caseId == {} API Request = {}", caseId, requestBody);
				apiResponseDTO = webClient.post().uri(env.getProperty(API_URL)).headers(h -> h.addAll(getHeaders()))
						.bodyValue(requestBody).retrieve().bodyToMono(ProcessResponse.class).block();

				log.info("caseId == {} API Response = {}", caseId, gson.toJson(apiResponseDTO));

				return apiResponseDTO; 

			} catch (Exception ex) {
				var handler = CustomExceptionHandler.handle(ex);
				log.error("caseId == {} {} {}", caseId, API_EXCEPTION, gson.toJson(handler));

				if (attempt == 3) { 
					return new ProcessResponse();
				}
			}
		}
		return apiResponseDTO; 
	}

	public HttpHeaders getHeaders() {
		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.setContentType(MediaType.APPLICATION_JSON);
		requestHeaders.setAccept(Arrays.asList(MediaType.ALL));
		requestHeaders.add(OCP_KEY_HEADER, env.getProperty(OCP_SUB_KEY));
		requestHeaders.add(AUTHORIZATION_HEADER, BEARER + azureTokenResponse.getAccess_token());
		return requestHeaders;
	}

	public synchronized AzureTokenResponse getToken() throws Exception {
		try {
			if (azureTokenResponse == null) {
				log.info("Getting Authentication from Gateway");

				// Build form parameters
				MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
				params.add(CLIENT_ID, env.getProperty(AUTH_CLIENT_ID));
				params.add(CLIENT_SECRET, env.getProperty(AUTH_CLIENT_SECRET));
				params.add(GRANT_TYPE, CLIENT_CREDENTIALS);
				params.add(RESOURCE, RESOURCE_URL);

				// Send POST request
				azureTokenResponse = webClient.post().uri(env.getProperty(AUTH_TOKEN_URL)).bodyValue(params).retrieve()
						.bodyToMono(AzureTokenResponse.class).block();

				log.info("Received Auth Response from Gateway == {}", gson.toJson(azureTokenResponse));
			}
			return azureTokenResponse;
		} catch (Exception e) {
			throw e;
		}
	}
}
