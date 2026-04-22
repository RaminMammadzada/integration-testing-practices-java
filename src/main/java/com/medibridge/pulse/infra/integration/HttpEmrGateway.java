package com.medibridge.pulse.infra.integration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
@ConditionalOnProperty(value = "integration.emr.enabled", havingValue = "true")
public class HttpEmrGateway implements EmrGateway {

    private final RestClient restClient;

    public HttpEmrGateway(@Value("${integration.emr.base-url}") String baseUrl, RestClient.Builder builder) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    @Override
    public void exportDocument(String eventType, String payload, String correlationId) {
        restClient.post()
                .uri("/documents")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("eventType", eventType, "payload", payload, "correlationId", correlationId))
                .retrieve()
                .toBodilessEntity();
    }
}
