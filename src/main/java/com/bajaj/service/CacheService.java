package com.bajaj.service;

import com.bajaj.config.AppProperties;
import com.bajaj.dto.ConfigDto;
import com.bajaj.dto.ProcessRequest;
import com.bajaj.dto.ProcessResponse;
import com.bajaj.entity.BaseTransaction;
import com.bajaj.entity.BureauTransaction;
import com.bajaj.entity.DedupeTransaction;
import com.bajaj.exception.DownstreamException;
import com.bajaj.exception.ServiceNotSupportedException;
import com.bajaj.repository.BureauTransactionRepository;
import com.bajaj.repository.DedupeTransactionRepository;
import com.bajaj.util.ProcessTimingContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

@Service
@RequiredArgsConstructor
public class CacheService {

    private final HashService hashService;
    private final ObjectMapper objectMapper;
    private final AppProperties props;
    private final BureauTransactionRepository bureauRepo;
    private final DedupeTransactionRepository dedupeRepo;
    private final DownstreamClient downstream;

    @Transactional
    public ProcessResponse process(ProcessRequest request, ProcessTimingContext timing) {
        ConfigDto config = request.getConfig();
        if (config == null || config.getServiceName() == null || config.getServiceName().isBlank()) {
            throw new ServiceNotSupportedException("config.serviceName is required (Bureau or dedupe)");
        }
        if (request.getData() == null) {
            throw new ServiceNotSupportedException("data block is required");
        }

        timing.setServiceName(config.getServiceName());

        return switch (config.getServiceName().trim().toLowerCase()) {
            case "bureau" -> handle(request, "BUREAU", timing,
                    bureauRepo::findTopByRequestHashOrderByResponseTimestampDesc,
                    bureauRepo::save, BureauTransaction::new, downstream::callBureau);
            case "dedupe" -> handle(request, "DEDUPE", timing,
                    dedupeRepo::findTopByRequestHashOrderByResponseTimestampDesc,
                    dedupeRepo::save, DedupeTransaction::new, downstream::callDedupe);
            default -> throw new ServiceNotSupportedException(
                    "Unsupported serviceName='" + config.getServiceName() + "'. Allowed: Bureau, dedupe.");
        };
    }

    private <T extends BaseTransaction> ProcessResponse handle(
            ProcessRequest request, String btId, ProcessTimingContext timing,
            Function<String, Optional<T>> finder,
            UnaryOperator<T> saver,
            Supplier<T> factory,
            Function<ProcessRequest, ProcessResponse> caller) {

        String hash = hashService.sha256(request.getData());
        timing.setBtId(btId);
        timing.setRequestHash(hash);

        timing.markDbSearchStart();
        Optional<T> existing = finder.apply(hash);
        boolean found = existing.isPresent();

        if (found && isFresh(existing.get())) {
            timing.markDbSearchEnd(true, "FRESH_HIT");
            return response(request, readJson(existing.get().getResponseJson()));
        }

        String outcome = found ? "STALE_HIT" : "MISS";
        timing.markDbSearchEnd(found, outcome);

        timing.markDownstreamCallStart();
        Instant requestTs = Instant.now();
        ProcessResponse downstreamResponse = caller.apply(request);
        timing.markDownstreamCallEnd();

        if (downstreamResponse == null || downstreamResponse.getData() == null) {
            throw new DownstreamException("DOWNSTREAM_DATA_MISSING",
                    btId + " downstream response does not contain data", 502);
        }
        JsonNode body = downstreamResponse.getData();
        Instant responseTs = Instant.now();

        timing.markDbSaveStart();
        T row = existing.orElseGet(factory);
        row.setRequestJson(writeJson(request));
        row.setRequestHash(hash);
        row.setResponseJson(writeJson(body));
        row.setStatus("SUCCESS");
        row.setRequestTimestamp(requestTs);
        row.setResponseTimestamp(responseTs);
        row.setBtId(btId);
        saver.apply(row);
        timing.markDbSaveEnd();

        return ProcessResponse.builder()
                .config(resolveResponseConfig(request, downstreamResponse))
                .data(body)
                .build();
    }

    private boolean isFresh(BaseTransaction row) {
        Instant ts = row.getResponseTimestamp();
        return ts != null && Duration.between(ts, Instant.now()).toDays() < props.getCache().getStalenessDays();
    }

    private static ProcessResponse response(ProcessRequest req, JsonNode data) {
        return ProcessResponse.builder().config(req.getConfig()).data(data).build();
    }

    private static ConfigDto resolveResponseConfig(ProcessRequest request, ProcessResponse downstreamResponse) {
        if (downstreamResponse != null && downstreamResponse.getConfig() != null) {
            return downstreamResponse.getConfig();
        }
        return request.getConfig();
    }

    private byte[] writeJson(Object value) {
        try {
            return objectMapper.writeValueAsBytes(value);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize JSON", e);
        }
    }

    private JsonNode readJson(byte[] bytes) {
        if (bytes == null || bytes.length == 0) return objectMapper.nullNode();
        try {
            return objectMapper.readTree(new String(bytes, StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse cached response", e);
        }
    }
}
