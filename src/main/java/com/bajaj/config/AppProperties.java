package com.bajaj.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private Cache cache = new Cache();
    private Downstream downstream = new Downstream();

    @Data
    public static class Cache {
        private int stalenessDays = 30;
    }

    @Data
    public static class Downstream {
        private String bureauUrl;
        private String dedupeUrl;
        private int connectTimeoutMs = 3000;
        private int readTimeoutMs = 15000;
    }
}
