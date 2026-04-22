package com.medibridge.pulse.infra.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DomainEventRepository extends JpaRepository<DomainEventEntity, UUID> {
}
