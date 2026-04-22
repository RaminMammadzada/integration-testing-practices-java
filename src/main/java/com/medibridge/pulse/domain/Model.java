package com.medibridge.pulse.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class Model {

    private Model() {
    }

    public enum DeviceStatus {
        REGISTERED, AVAILABLE, ASSIGNED, INFUSING, PAUSED, ALARMING, MAINTENANCE_REQUIRED, OFFLINE, DECOMMISSIONED
    }

    public enum AlarmSeverity {
        INFO, LOW, MEDIUM, HIGH, CRITICAL
    }

    public enum TherapyState {
        APPROVED, READY, RUNNING, PAUSED, COMPLETED, ABORTED
    }

    public enum MaintenanceStatus {
        OPEN, IN_PROGRESS, RESOLVED
    }

    public record Device(
            UUID id,
            String serialNumber,
            String model,
            String firmwareVersion,
            String hospital,
            String ward,
            String bed,
            DeviceStatus status,
            int batteryLevel,
            int wifiStrength,
            Instant lastHeartbeat
    ) {
    }

    public record DeviceAssignment(
            UUID id,
            UUID deviceId,
            String patientId,
            String careUnit,
            String nurse,
            Instant assignedAt,
            Instant releasedAt
    ) {
    }

    public record InfusionOrder(
            UUID id,
            String medication,
            String dose,
            String rate,
            String patientId,
            String physician,
            String pharmacyApproval
    ) {
    }

    public record TherapySession(
            UUID id,
            UUID orderId,
            UUID deviceId,
            TherapyState state,
            Instant startedAt,
            Instant endedAt,
            double infusedVolumeMl,
            double remainingVolumeMl
    ) {
    }

    public record DrugLibrary(
            UUID id,
            String libraryName,
            String version,
            String hospital,
            String approvalStatus,
            List<String> medications
    ) {
    }

    public record Alarm(
            UUID id,
            UUID deviceId,
            String patientId,
            AlarmSeverity severity,
            String category,
            String message,
            Instant raisedAt,
            Instant acknowledgedAt,
            String acknowledgedBy
    ) {
    }

    public record EmrDocument(
            UUID id,
            UUID therapySessionId,
            String eventType,
            String payload,
            String exportStatus,
            int retryCount,
            Instant createdAt
    ) {
    }

    public record MaintenanceTicket(
            UUID id,
            UUID deviceId,
            String issueType,
            String priority,
            MaintenanceStatus status,
            LocalDate dueDate,
            String assignee
    ) {
    }

    public record AuditEvent(
            UUID id,
            String actor,
            String action,
            String resource,
            String correlationId,
            Instant timestamp
    ) {
    }

    public record PlatformHealth(
            String status,
            int registeredDevices,
            long activeTherapies,
            long activeAlarms,
            long openMaintenanceTickets,
            Instant generatedAt
    ) {
    }

    public record DomainEvent(
            UUID id,
            String type,
            String producer,
            String correlationId,
            Instant occurredAt,
            String payload
    ) {
    }
}
