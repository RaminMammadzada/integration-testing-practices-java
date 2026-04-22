package com.medibridge.pulse.infra.integration;

import com.medibridge.pulse.domain.Model;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(value = "integration.kafka.enabled", havingValue = "true")
public class KafkaDomainEventPublisher implements DomainEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaDomainEventPublisher(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publish(Model.DomainEvent event) {
        kafkaTemplate.send("medibridge.domain-events", event.id().toString(), event.type() + "|" + event.payload());
    }
}
