package com.bajaj.controller;

import com.bajaj.dto.EncryptedRequestEnvelope;
import com.bajaj.dto.EncryptedResponseEnvelope;
import com.bajaj.dto.ProcessRequest;
import com.bajaj.dto.ProcessResponse;
import com.bajaj.exception.BadRequestException;
import com.bajaj.exception.CryptoException;
import com.bajaj.security.EncryptionAspect;
import com.bajaj.service.CacheService;
import com.bajaj.service.UslBreTransactionService;
import com.bajaj.util.ProcessTimingContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class USLController {

    private final CacheService cacheService;
    private final UslBreTransactionService uslBreTransactionService;
    private final ObjectMapper objectMapper;

    @PostMapping(value = "/service",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EncryptedResponseEnvelope> process(@Valid @RequestBody EncryptedRequestEnvelope envelope) {
        long start = System.currentTimeMillis();
        ProcessTimingContext timing = new ProcessTimingContext(
                envelope.getReferenceId(), envelope.getSourceSystem());

        ProcessRequest request = parse(decrypt(envelope.getRequest()));
        if (request.getConfig() != null) {
            timing.setServiceName(request.getConfig().getServiceName());
        }

        ProcessResponse response = cacheService.process(request, timing);
        String encrypted = encrypt(response);
        timing.markResponseReturned();
        uslBreTransactionService.record(request, response, timing, "SUCCESS");

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
