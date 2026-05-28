package com.bajaj.service;

import com.bajaj.config.AppProperties;
import com.bajaj.dto.ProcessRequest;
import com.bajaj.dto.ProcessResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DownstreamClient {

    private final AppProperties props;
    private final WebClientService webClientService;

    public ProcessResponse callBureau(ProcessRequest request) {
        return call("BUREAU", props.getDownstream().getBureauUrl(), request);
    }

    public ProcessResponse callDedupe(ProcessRequest request) {
        return call("DEDUPE", props.getDownstream().getDedupeUrl(), request);
    }

    private ProcessResponse call(String label, String url, ProcessRequest request) {
        return webClientService.postEncrypted(request, url, label);
    }
}
