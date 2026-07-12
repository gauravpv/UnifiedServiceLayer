package com.bajaj.util;

import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public final class ProcessTimingContext {

    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS").withZone(ZoneId.systemDefault());

    private final String correlationId;
    private final String sourceSystem;
    private final Instant requestLandedAt;

    private String serviceName = "unknown";
    private String btId;
    private String requestHash;
    private Instant dbSearchStartAt;
    private Instant dbSearchEndAt;
    private String cacheOutcome;
    private Instant downstreamCallStartAt;
    private Instant downstreamCallEndAt;
    private Instant dbSaveStartAt;
    private Instant dbSaveEndAt;
    private Instant responseReturnedAt;

    public ProcessTimingContext(String referenceId, String sourceSystem) {
        this.sourceSystem = sourceSystem == null ? "unknown" : sourceSystem;
        this.correlationId = (referenceId == null || referenceId.isBlank()) ? "no-ref" : referenceId;
        this.requestLandedAt = Instant.now();
    }

    public void setServiceName(String serviceName) {
        if (serviceName != null && !serviceName.isBlank()) {
            this.serviceName = serviceName;
        }
    }

    public void setBtId(String btId) {
        if (btId != null && !btId.isBlank()) {
            this.btId = btId;
        }
    }

    public void setRequestHash(String requestHash) {
        if (requestHash != null && !requestHash.isBlank()) {
            this.requestHash = requestHash;
        }
    }

    public TimingSnapshot snapshot() {
        Instant end = responseReturnedAt == null ? Instant.now() : responseReturnedAt;
        long totalMs = Duration.between(requestLandedAt, end).toMillis();
        long serviceMs = durationMs(downstreamCallStartAt, downstreamCallEndAt);
        long systemMs = totalMs - serviceMs;
        return new TimingSnapshot(
                correlationId,
                sourceSystem,
                requestLandedAt,
                end,
                totalMs,
                systemMs,
                serviceMs,
                cacheOutcome,
                btId,
                requestHash);
    }

    public void markDbSearchStart() {
        dbSearchStartAt = Instant.now();
    }

    public void markDbSearchEnd(boolean found, String outcome) {
        dbSearchEndAt = Instant.now();
        cacheOutcome = outcome;
    }

    public void markDownstreamCallStart() {
        downstreamCallStartAt = Instant.now();
    }

    public void markDownstreamCallEnd() {
        downstreamCallEndAt = Instant.now();
    }

    public void markDbSaveStart() {
        dbSaveStartAt = Instant.now();
    }

    public void markDbSaveEnd() {
        dbSaveEndAt = Instant.now();
    }

    public void markResponseReturned() {
        responseReturnedAt = Instant.now();
        log.info("\n{}", buildReport());
    }

    private String buildReport() {
        Instant end = responseReturnedAt == null ? Instant.now() : responseReturnedAt;
        long totalMs = Duration.between(requestLandedAt, end).toMillis();
        long dbLookupMs = durationMs(dbSearchStartAt, dbSearchEndAt);
        long downstreamMs = durationMs(downstreamCallStartAt, downstreamCallEndAt);
        long dbSaveMs = durationMs(dbSaveStartAt, dbSaveEndAt);
        long systemMs = totalMs - downstreamMs;

        List<TimelineStep> steps = new ArrayList<>();
        steps.add(new TimelineStep("Request Landed", requestLandedAt, null));
        if (dbSearchStartAt != null) {
            steps.add(new TimelineStep("DB Search Started", dbSearchStartAt, null));
        }
        if (dbSearchEndAt != null) {
            steps.add(new TimelineStep("DB Search Completed", dbSearchEndAt, dbLookupMs));
        }
        if (downstreamCallStartAt != null) {
            steps.add(new TimelineStep("Downstream Call Started", downstreamCallStartAt, null));
        }
        if (downstreamCallEndAt != null) {
            steps.add(new TimelineStep("Downstream Call Completed", downstreamCallEndAt, downstreamMs));
        }
        if (dbSaveStartAt != null) {
            steps.add(new TimelineStep("DB Save Started", dbSaveStartAt, null));
        }
        if (dbSaveEndAt != null) {
            steps.add(new TimelineStep("DB Save Completed", dbSaveEndAt, dbSaveMs));
        }
        steps.add(new TimelineStep("Response Returned", end, null));

        String line = "================================================================";
        String divider = "----------------------------------------------------------------";
        StringBuilder report = new StringBuilder();
        report.append(line).append('\n');
        report.append(" REQUEST PROCESS TIMELINE\n");
        report.append(line).append('\n');
        report.append(String.format(" Reference ID     : %s%n", correlationId));
        report.append(String.format(" Source System    : %s%n", sourceSystem));
        report.append(String.format(" Service          : %s%n", serviceName));
        report.append(String.format(" Cache Outcome    : %s%n", cacheOutcome == null ? "N/A" : cacheOutcome));
        report.append(divider).append('\n');
        report.append(String.format(" %-3s %-28s %-26s %s%n", "#", "Step", "Timestamp", "Duration"));
        report.append(divider).append('\n');

        for (int i = 0; i < steps.size(); i++) {
            TimelineStep step = steps.get(i);
            report.append(String.format(" %-3d %-28s %-26s %s%n",
                    i + 1,
                    step.label(),
                    format(step.timestamp()),
                    step.durationMs() == null ? "-" : step.durationMs() + " ms"));
        }

        report.append(divider).append('\n');
        report.append(String.format(" %-32s %s%n", "Total Time", totalMs + " ms"));
        report.append(String.format(" %-32s %s%n", "System Time (excl. service)", systemMs + " ms"));
        report.append(String.format(" %-32s %s%n", "Downstream Call Time", downstreamMs + " ms"));
        report.append(String.format(" %-32s %s%n", "DB Lookup Time", dbLookupMs + " ms"));
        report.append(String.format(" %-32s %s%n", "DB Save Time", dbSaveMs + " ms"));
        report.append(line);
        return report.toString();
    }

    private static long durationMs(Instant start, Instant end) {
        return start == null || end == null ? 0 : Duration.between(start, end).toMillis();
    }

    private static String format(Instant instant) {
        return TIMESTAMP_FORMAT.format(instant);
    }

    private record TimelineStep(String label, Instant timestamp, Long durationMs) {}

    public record TimingSnapshot(
            String referenceId,
            String sourceSystem,
            Instant requestTimestamp,
            Instant responseTimestamp,
            long totalMs,
            long systemMs,
            long serviceMs,
            String cacheOutcome,
            String btId,
            String requestHash) {}
}
