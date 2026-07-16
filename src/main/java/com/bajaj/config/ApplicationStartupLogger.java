package com.bajaj.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApplicationStartupLogger {

    private final AppProperties appProperties;
    private final Environment environment;

    @Value("${logging.file.name:logs/application.log}")
    private String logFile;

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        String appName = environment.getProperty("spring.application.name", "application");
        String port = environment.getProperty("local.server.port",
                environment.getProperty("server.port", "8080"));

        log.info("========================================");
        log.info("Application started: {}", appName);
        log.info("Server port: {}", port);
        log.info("Log file: {}", Paths.get(logFile).toAbsolutePath());
        log.info("Bureau URL: {}", appProperties.getDownstream().getBureauUrl());
        log.info("Dedupe URL: {}", appProperties.getDownstream().getDedupeUrl());
        log.info("Cache staleness days: {}", appProperties.getCache().getStalenessDays());
        AppProperties.Gateway gateway = appProperties.getGateway();
        log.info("Gateway OCP key configured: {}", gateway.hasOcpSubKey());
        log.info("Gateway token configured: {}", gateway.hasTokenCredentials());
        if (gateway.hasTokenCredentials()) {
            log.info("Gateway token URL: {}", gateway.getAuthTokenUrl());
        }
        log.info("========================================");
    }
}
