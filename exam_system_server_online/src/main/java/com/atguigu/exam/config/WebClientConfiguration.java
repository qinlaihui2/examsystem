package com.atguigu.exam.config;

import com.atguigu.exam.config.properties.KimiApiproperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@EnableConfigurationProperties(KimiApiproperties.class)
public class WebClientConfiguration {
/**
 * Creates and configures a WebClient bean instance using the provided KimiApiProperties.
 *
 * @param kimiApiproperties the configuration properties for Kimi API
 * @return a configured WebClient instance ready to use
 */

    @Autowired
    private KimiApiproperties kimiApiproperties;

    @Bean
    public WebClient webClient() {
        WebClient webClient = WebClient.builder().
                baseUrl("https://api.moonshot.cn/v1/chat/completions")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("Authorization", "Bearer " + kimiApiproperties.getApiKey())
                .build();
        return webClient;
    }
    // Method body would be implemented here
}
