package com.bajaj.service;

import com.bajaj.config.AppProperties;
import com.bajaj.dto.ProcessRequest;
import com.bajaj.exception.DownstreamException;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class DownstreamClient {

    private final RestClient restClient;
    private final AppProperties props;

    public JsonNode callBureau(ProcessRequest request) {
        return call("BUREAU", props.getDownstream().getBureauUrl(), request);
    }

    public JsonNode callDedupe(ProcessRequest request) {
        return call("DEDUPE", props.getDownstream().getDedupeUrl(), request);
    }

    private JsonNode call(String label, String url, ProcessRequest request) {
        if (url == null || url.isBlank()) {
            throw new DownstreamException("DOWNSTREAM_NOT_CONFIGURED",
                    label + " URL is not configured", 503);
        }
        log.info("[{}] POST {}", label, url);
        try {
            JsonNode body = restClient.post().uri(url).body(request).retrieve().body(JsonNode.class);
            if (body == null) {
                throw new DownstreamException("DOWNSTREAM_EMPTY_BODY", label + " returned empty body", 502);
            }
            return body;
        } catch (HttpClientErrorException e) {
            throw new DownstreamException("DOWNSTREAM_CLIENT_ERROR",
                    label + " returned " + e.getStatusCode(), e.getStatusCode().value(), e);
        } catch (HttpServerErrorException e) {
            throw new DownstreamException("DOWNSTREAM_SERVER_ERROR",
                    label + " returned " + e.getStatusCode(), e.getStatusCode().value(), e);
        } catch (ResourceAccessException e) {
            throw new DownstreamException("DOWNSTREAM_TIMEOUT",
                    label + " network/timeout: " + e.getMessage(), 504, e);
        }
    }
}
