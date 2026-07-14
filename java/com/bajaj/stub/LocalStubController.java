package com.bajaj.stub;

import com.bajaj.dto.EncryptedRequestEnvelope;
import com.bajaj.dto.EncryptedResponseEnvelope;
import com.bajaj.dto.ProcessRequest;
import com.bajaj.dto.ProcessResponse;
import com.bajaj.exception.BadRequestException;
import com.bajaj.exception.CryptoException;
import com.bajaj.security.EncryptionAspect;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Profile("local-stub")
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/stub", produces = MediaType.APPLICATION_JSON_VALUE)
public class LocalStubController {

    private final ObjectMapper objectMapper;

    @PostMapping("/bureau")
    public EncryptedResponseEnvelope bureau(@RequestBody EncryptedRequestEnvelope envelope) {
        log.info("[STUB] /stub/bureau hit");
        return buildEncryptedStubResponse(envelope, "BUREAU");
    }

    @PostMapping("/dedupe")
    public EncryptedResponseEnvelope dedupe(@RequestBody EncryptedRequestEnvelope envelope) {
        log.info("[STUB] /stub/dedupe hit");
        return buildEncryptedStubResponse(envelope, "DEDUPE");
    }

    private EncryptedResponseEnvelope buildEncryptedStubResponse(EncryptedRequestEnvelope envelope, String service) {
        ProcessRequest request = parseRequest(envelope);
        JsonNode mockedData = mockData(service, request.getData());
        ProcessResponse response = ProcessResponse.builder()
                .config(request.getConfig())
                .data(mockedData)
                .build();

        String encryptedResponse = encryptResponse(response);
        return EncryptedResponseEnvelope.builder()
                .response(encryptedResponse)
                .statusCode("200")
                .message("Success")
                .breTat("0ms")
                .build();
    }

    private ProcessRequest parseRequest(EncryptedRequestEnvelope envelope) {
        if (envelope == null || envelope.getRequest() == null || envelope.getRequest().isBlank()) {
            throw new BadRequestException("request is required");
        }
        try {
            String decrypted = EncryptionAspect.decrypt(envelope.getRequest());
            return objectMapper.readValue(decrypted, ProcessRequest.class);
        } catch (Exception e) {
            throw new BadRequestException("Invalid encrypted request payload");
        }
    }

    private String encryptResponse(ProcessResponse response) {
        try {
            return EncryptionAspect.encrypt(objectMapper.writeValueAsString(response));
        } catch (Exception e) {
            throw new CryptoException("Failed to encrypt stub response", e);
        }
    }

    private JsonNode mockData(String service, JsonNode inputData) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("source", "LOCAL_STUB");
        out.put("service", service);
        out.put("generatedAt", Instant.now().toString());
        if ("BUREAU".equals(service)) {
            out.put("score", 752);
            out.put("scoreBand", "GOOD");
            out.put("activeAccounts", 4);
            out.put("delinquencies", 0);
        } else {
            out.put("matchFound", false);
            out.put("matches", List.of());
        }
        out.put("echo", inputData);
        return objectMapper.valueToTree(out);
    }
}