package com.bajaj.service;

import static com.bajaj.constants.Constants.AUTHORIZATION_HEADER;
import static com.bajaj.constants.Constants.AUTH_CLIENT_ID;
import static com.bajaj.constants.Constants.AUTH_CLIENT_SECRET;
import static com.bajaj.constants.Constants.AUTH_TOKEN_URL;
import static com.bajaj.constants.Constants.BEARER;
import static com.bajaj.constants.Constants.CLIENT_CREDENTIALS;
import static com.bajaj.constants.Constants.CLIENT_ID;
import static com.bajaj.constants.Constants.CLIENT_SECRET;
import static com.bajaj.constants.Constants.GRANT_TYPE;
import static com.bajaj.constants.Constants.OCP_KEY_HEADER;
import static com.bajaj.constants.Constants.OCP_SUB_KEY;
import static com.bajaj.constants.Constants.RESOURCE;
import static com.bajaj.constants.Constants.RESOURCE_URL;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.bajaj.dto.AzureTokenResponse;
import com.bajaj.dto.ConfigDto;
import com.bajaj.dto.DownstreamEncryptedRequest;
import com.bajaj.dto.EncryptedResponseEnvelope;
import com.bajaj.dto.ProcessRequest;
import com.bajaj.dto.ProcessResponse;
import com.bajaj.exception.CryptoException;
import com.bajaj.exception.DownstreamException;
import com.bajaj.security.EncryptionAspect;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebClientService {

    private final WebClient.Builder webClientBuilder;
    private final Environment env;
    private final ObjectMapper objectMapper;

    private volatile AzureTokenResponse azureTokenResponse;
    private volatile Instant tokenExpiresAt = Instant.MIN;

    @SuppressWarnings("null")
    public ProcessResponse postEncrypted(ProcessRequest request, String url, String label) {
        if (url == null || url.isBlank()) {
            throw new DownstreamException("DOWNSTREAM_NOT_CONFIGURED", label + " URL is not configured", 503);
        }

        try {
            ProcessRequest outboundRequest = buildOutboundRequest(request);
            String plaintext = objectMapper.writeValueAsString(outboundRequest);
            String encryptedPayload = EncryptionAspect.encrypt(plaintext);

            DownstreamEncryptedRequest envelope = DownstreamEncryptedRequest.builder()
                    .request(encryptedPayload)
                    .build();
            HttpHeaders outboundHeaders = getHeaders();

            log.info("[{}] POST {} (encrypted request envelope)", label, url);
            EncryptedResponseEnvelope encryptedResponse = webClientBuilder.build()
                    .post()
                    .uri(url)
                    .headers(headers -> headers.addAll(outboundHeaders))
                    .bodyValue(Objects.requireNonNull(envelope))
                    .retrieve()
                    .bodyToMono(EncryptedResponseEnvelope.class)
                    .block();

            if (encryptedResponse == null) {
                throw new DownstreamException("DOWNSTREAM_EMPTY_BODY", label + " returned empty body", 502);
            }
            return decryptDownstreamResponse(encryptedResponse, label);
        } catch (DownstreamException e) {
            throw e;
        } catch (CryptoException e) {
            throw e;
        } catch (org.springframework.web.reactive.function.client.WebClientResponseException e) {
            throw new DownstreamException("DOWNSTREAM_CLIENT_ERROR",
                    label + " returned " + e.getStatusCode(), e.getStatusCode().value(), e);
        } catch (org.springframework.web.reactive.function.client.WebClientRequestException e) {
            throw new DownstreamException("DOWNSTREAM_TIMEOUT",
                    label + " network/timeout: " + e.getMessage(), 504, e);
        } catch (Exception e) {
            throw new DownstreamException("DOWNSTREAM_ERROR",
                    label + " call failed: " + e.getMessage(), 502, e);
        }
    }

    private ProcessRequest buildOutboundRequest(ProcessRequest inbound) {
        ConfigDto inCfg = inbound.getConfig() == null ? new ConfigDto() : inbound.getConfig();
        ConfigDto outCfg = ConfigDto.builder()
                .orgName(inCfg.getOrgName())
                .channelName("USL")
                .productName(inCfg.getProductName())
                .requestId(inCfg.getRequestId())
                .caseId(inCfg.getCaseId())
                .requestedVersion(inCfg.getRequestedVersion())
                .requestTimestamp(inCfg.getRequestTimestamp())
                .build();

        return ProcessRequest.builder()
                .config(outCfg)
                .data(inbound.getData())
                .build();
    }

    private ProcessResponse decryptDownstreamResponse(EncryptedResponseEnvelope encryptedResponse, String label) {
        if (isBlank(encryptedResponse.getResponse())) {
            throw new DownstreamException("DOWNSTREAM_ENCRYPTED_RESPONSE_MISSING",
                    label + " response envelope missing encrypted response", 502);
        }

        try {
            String decrypted = EncryptionAspect.decrypt(encryptedResponse.getResponse());
            ProcessResponse decryptedResponse = objectMapper.readValue(decrypted, ProcessResponse.class);
            if (decryptedResponse == null || decryptedResponse.getData() == null) {
                throw new DownstreamException("DOWNSTREAM_DATA_MISSING",
                        label + " decrypted response does not contain data", 502);
            }
            return decryptedResponse;
        } catch (DownstreamException e) {
            throw e;
        } catch (Exception e) {
            throw new CryptoException("Failed to decrypt downstream response", e);
        }
    }

    @SuppressWarnings("null")
    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        Optional.ofNullable(env.getProperty(OCP_SUB_KEY))
                .filter(value -> !value.isBlank())
                .ifPresent(value -> headers.add(OCP_KEY_HEADER, value));
        getToken().ifPresent(token -> headers.add(AUTHORIZATION_HEADER, BEARER + token.getAccess_token()));
        return headers;
    }

    @SuppressWarnings("null")
    private Optional<AzureTokenResponse> getToken() {
        String tokenUrl = env.getProperty(AUTH_TOKEN_URL);
        String clientId = env.getProperty(AUTH_CLIENT_ID);
        String clientSecret = env.getProperty(AUTH_CLIENT_SECRET);
        if (isBlank(tokenUrl) || isBlank(clientId) || isBlank(clientSecret)) {
            return Optional.empty();
        }
        tokenUrl = Objects.requireNonNull(tokenUrl);
        clientId = Objects.requireNonNull(clientId);
        clientSecret = Objects.requireNonNull(clientSecret);

        if (azureTokenResponse != null && Instant.now().isBefore(tokenExpiresAt)) {
            return Optional.of(azureTokenResponse);
        }

        synchronized (this) {
            if (azureTokenResponse != null && Instant.now().isBefore(tokenExpiresAt)) {
                return Optional.of(azureTokenResponse);
            }

            log.info("Fetching gateway token");
            AzureTokenResponse token = webClientBuilder.build()
                    .post()
                    .uri(tokenUrl)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters
                            .fromFormData(CLIENT_ID, clientId)
                            .with(CLIENT_SECRET, clientSecret)
                            .with(GRANT_TYPE, CLIENT_CREDENTIALS)
                            .with(RESOURCE, RESOURCE_URL))
                    .retrieve()
                    .bodyToMono(AzureTokenResponse.class)
                    .block();

            if (token == null || isBlank(token.getAccess_token())) {
                throw new DownstreamException("AUTH_TOKEN_ERROR", "Gateway token response missing access_token", 502);
            }

            azureTokenResponse = token;
            int expiresIn = parseExpiresIn(token.getExpires_in());
            tokenExpiresAt = Instant.now().plusSeconds(Math.max(30, expiresIn - 60L));
            return Optional.of(azureTokenResponse);
        }
    }

    private static int parseExpiresIn(String expiresIn) {
        try {
            return Integer.parseInt(expiresIn);
        } catch (Exception ignored) {
            return 300;
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
