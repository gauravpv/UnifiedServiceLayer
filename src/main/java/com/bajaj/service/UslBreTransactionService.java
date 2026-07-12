package com.bajaj.service;

import com.bajaj.dto.ProcessRequest;
import com.bajaj.dto.ProcessResponse;
import com.bajaj.entity.UslBreTransaction;
import com.bajaj.repository.UslBreTransactionRepository;
import com.bajaj.util.DurationParts;
import com.bajaj.util.ProcessTimingContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UslBreTransactionService {

    private final UslBreTransactionRepository repository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void record(ProcessRequest request, ProcessResponse response, ProcessTimingContext timing, String status) {
        try {
            ProcessTimingContext.TimingSnapshot snapshot = timing.snapshot();
            String totalTime = DurationParts.formatMillis(snapshot.totalMs());
            String systemTime = DurationParts.formatMillis(snapshot.systemMs());
            String serviceTime = DurationParts.formatMillis(snapshot.serviceMs());

            UslBreTransaction row = new UslBreTransaction();
            row.setReferenceId(snapshot.referenceId());
            row.setSourceSystem(snapshot.sourceSystem());
            row.setRequestJson(writeJson(request));
            row.setRequestHash(snapshot.requestHash());
            row.setResponseJson(writeJson(response));
            row.setStatus(status);
            row.setRequestTimestamp(snapshot.requestTimestamp());
            row.setResponseTimestamp(snapshot.responseTimestamp());
            row.setBtId(snapshot.btId());
            row.setCacheOutcome(snapshot.cacheOutcome());
            row.setTotalTime(totalTime);
            row.setSystemTime(systemTime);
            row.setServiceTime(serviceTime);

            repository.save(row);
            log.info("[{}] Saved usl_bre_transactions id={} bt_id={} total={} system={} service={}",
                    snapshot.referenceId(), row.getId(), snapshot.btId(),
                    totalTime, systemTime, serviceTime);
        } catch (Exception e) {
            log.error("Failed to save usl_bre_transactions audit row", e);
        }
    }

    private byte[] writeJson(Object value) {
        try {
            return objectMapper.writeValueAsBytes(value);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize audit JSON", e);
        }
    }
}
