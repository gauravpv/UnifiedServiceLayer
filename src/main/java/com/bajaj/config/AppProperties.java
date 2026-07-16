package com.bajaj.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private Cache cache = new Cache();
    private Downstream downstream = new Downstream();
    private Gateway gateway = new Gateway();

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

    @Data
    public static class Gateway {
        private String ocpSubKey;
        private String authClientId;
        private String authClientSecret;
        private String authTokenUrl;

        public boolean hasOcpSubKey() {
            return hasText(ocpSubKey);
        }

        public boolean hasTokenCredentials() {
            return hasText(authTokenUrl) && hasText(authClientId) && hasText(authClientSecret);
        }

        private static boolean hasText(String value) {
            return value != null && !value.isBlank();
        }
    }
}
