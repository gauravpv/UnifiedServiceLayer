package com.bajaj.config;

import io.netty.channel.ChannelOption;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient downstreamWebClient(WebClient.Builder builder, AppProperties props) {
        AppProperties.Downstream downstream = props.getDownstream();
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofMillis(downstream.getReadTimeoutMs()))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, downstream.getConnectTimeoutMs());

        return builder
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
