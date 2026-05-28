package com.bajaj.stub;

import com.fasterxml.jackson.databind.JsonNode;
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
@RequestMapping(value = "/stub", produces = MediaType.APPLICATION_JSON_VALUE)
public class LocalStubController {

    @PostMapping("/bureau")
    public Map<String, Object> bureau(@RequestBody JsonNode body) {
        log.info("[STUB] /stub/bureau hit");
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("source", "LOCAL_STUB");
        out.put("service", "BUREAU");
        out.put("generatedAt", Instant.now().toString());
        out.put("score", 752);
        out.put("scoreBand", "GOOD");
        out.put("activeAccounts", 4);
        out.put("delinquencies", 0);
        out.put("echo", body);
        return out;
    }

    @PostMapping("/dedupe")
    public Map<String, Object> dedupe(@RequestBody JsonNode body) {
        log.info("[STUB] /stub/dedupe hit");
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("source", "LOCAL_STUB");
        out.put("service", "DEDUPE");
        out.put("generatedAt", Instant.now().toString());
        out.put("matchFound", false);
        out.put("matches", List.of());
        out.put("echo", body);
        return out;
    }
}