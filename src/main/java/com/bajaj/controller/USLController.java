package com.bajaj.controller;

import com.bajaj.dto.EncryptedRequestEnvelope;
import com.bajaj.dto.EncryptedResponseEnvelope;
import com.bajaj.dto.ProcessRequest;
import com.bajaj.dto.ProcessResponse;
import com.bajaj.exception.BadRequestException;
import com.bajaj.exception.CryptoException;
import com.bajaj.security.EncryptionAspect;
import com.bajaj.service.CacheService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class USLController {

    private final CacheService cacheService;
    private final ObjectMapper objectMapper;

    @PostMapping(value = "/service",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EncryptedResponseEnvelope> process(@Valid @RequestBody EncryptedRequestEnvelope envelope) {
        long start = System.currentTimeMillis();
        log.info("POST /api/v1/process reference_id={} source_system={}",
                envelope.getReferenceId(), envelope.getSourceSystem());

        ProcessRequest request = parse(decrypt(envelope.getRequest()));
        ProcessResponse response = cacheService.process(request);
        String encrypted = encrypt(response);

        return ResponseEntity.ok(EncryptedResponseEnvelope.builder()
                .response(encrypted)
                .statusCode("200")
                .message("Success")
                .breTat((System.currentTimeMillis() - start) + "ms")
                .build());
    }

    private String decrypt(String ciphertext) {
        try {
            return EncryptionAspect.decrypt(ciphertext);
        } catch (Exception e) {
            throw new CryptoException("Failed to decrypt request", e);
        }
    }

    private String encrypt(ProcessResponse response) {
        try {
            return EncryptionAspect.encrypt(objectMapper.writeValueAsString(response));
        } catch (Exception e) {
            throw new CryptoException("Failed to encrypt response", e);
        }
    }

    private ProcessRequest parse(String json) {
        try {
            return objectMapper.readValue(json, ProcessRequest.class);
        } catch (Exception e) {
            throw new BadRequestException("Decrypted payload is not valid JSON: " + e.getMessage());
        }
    }
}
