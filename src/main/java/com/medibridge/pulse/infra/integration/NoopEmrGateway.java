package com.medibridge.pulse.infra.integration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(value = "integration.emr.enabled", havingValue = "false", matchIfMissing = true)
public class NoopEmrGateway implements EmrGateway {
    @Override
    public void exportDocument(String eventType, String payload, String correlationId) {
        // no-op for local/dev mode
    }
}
