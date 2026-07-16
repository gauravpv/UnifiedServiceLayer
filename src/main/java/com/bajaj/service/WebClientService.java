package com.bajaj.service;

import static com.bajaj.constants.Constants.AUTHORIZATION_HEADER;
import static com.bajaj.constants.Constants.BEARER;
import static com.bajaj.constants.Constants.CLIENT_CREDENTIALS;
import static com.bajaj.constants.Constants.CLIENT_ID;
import static com.bajaj.constants.Constants.CLIENT_SECRET;
import static com.bajaj.constants.Constants.GRANT_TYPE;
import static com.bajaj.constants.Constants.OCP_KEY_HEADER;
import static com.bajaj.constants.Constants.RESOURCE;
import static com.bajaj.constants.Constants.RESOURCE_URL;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.bajaj.config.GatewayAuthConfig;
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

    private final WebClient downstreamWebClient;
    private final ObjectMapper objectMapper;
    private final GatewayAuthConfig gatewayAuth;

    private volatile AzureTokenResponse azureTokenResponse;
    private volatile Instant tokenExpiresAt = Instant.MIN;

    @SuppressWarnings("null")
    public ProcessResponse postEncrypted(ProcessRequest request, String url, String label) {
        if (url == null || url.isBlank()) {
            throw new DownstreamException("DOWNSTREAM_NOT_CONFIGURED", label + " URL is not configured", 503);
        }

        try {
            ProcessRequest outboundRequest = buildOutboundRequest(request);
            ConfigDto cfg = outboundRequest.getConfig();
            String requestId = cfg != null ? cfg.getRequestId() : null;
            String caseId = cfg != null ? cfg.getCaseId() : null;
            log.debug("[{}] Outbound request: {}", label, outboundRequest);

            String plaintext = objectMapper.writeValueAsString(outboundRequest);
            String encryptedPayload = EncryptionAspect.encrypt(plaintext);
            log.debug("[{}] Encrypted outbound request: {}", label, encryptedPayload);

            DownstreamEncryptedRequest envelope = DownstreamEncryptedRequest.builder()
                    .request(encryptedPayload)
                    .build();
            HttpHeaders outboundHeaders = getHeaders();
            log.debug("[{}] Outbound headers: {}", label, outboundHeaders);

            log.info("[{}] POST {} requestId={} caseId={}", label, url, requestId, caseId);
            EncryptedResponseEnvelope encryptedResponse = downstreamWebClient
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
                .sourceSystem("USL")
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

    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        if (gatewayAuth.hasOcpSubKey()) {
            headers.add(OCP_KEY_HEADER, gatewayAuth.ocpSubKey());
        }
        getToken().ifPresent(token -> headers.add(AUTHORIZATION_HEADER, BEARER + token.getAccess_token()));
        return headers;
    }

    @SuppressWarnings("null")
    private Optional<AzureTokenResponse> getToken() {
        if (!gatewayAuth.hasTokenCredentials()) {
            return Optional.empty();
        }

        String tokenUrl = gatewayAuth.authTokenUrl();
        String clientId = gatewayAuth.authClientId();
        String clientSecret = gatewayAuth.authClientSecret();
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

            log.debug("Fetching gateway token");
            AzureTokenResponse token = downstreamWebClient
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
