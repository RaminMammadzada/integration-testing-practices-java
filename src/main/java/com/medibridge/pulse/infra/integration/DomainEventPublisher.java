package com.medibridge.pulse.infra.integration;

import com.medibridge.pulse.domain.Model;

public interface DomainEventPublisher {
    void publish(Model.DomainEvent event);
}
