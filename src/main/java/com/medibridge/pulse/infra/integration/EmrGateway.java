package com.medibridge.pulse.infra.integration;

public interface EmrGateway {
    void exportDocument(String eventType, String payload, String correlationId);
}
