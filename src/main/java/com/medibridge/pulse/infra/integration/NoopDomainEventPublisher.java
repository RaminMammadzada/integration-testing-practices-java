package com.medibridge.pulse.infra.integration;

import com.medibridge.pulse.domain.Model;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(value = "integration.kafka.enabled", havingValue = "false", matchIfMissing = true)
public class NoopDomainEventPublisher implements DomainEventPublisher {

    @Override
    public void publish(Model.DomainEvent event) {
        // no-op for local/dev mode
    }
}
