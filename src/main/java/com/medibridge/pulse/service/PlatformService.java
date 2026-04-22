package com.medibridge.pulse.service;

import com.medibridge.pulse.domain.Model;
import com.medibridge.pulse.domain.Model.AlarmSeverity;
import com.medibridge.pulse.domain.Model.Device;
import com.medibridge.pulse.domain.Model.DeviceAssignment;
import com.medibridge.pulse.domain.Model.DeviceStatus;
import com.medibridge.pulse.domain.Model.DomainEvent;
import com.medibridge.pulse.domain.Model.DrugLibrary;
import com.medibridge.pulse.domain.Model.EmrDocument;
import com.medibridge.pulse.domain.Model.InfusionOrder;
import com.medibridge.pulse.domain.Model.MaintenanceStatus;
import com.medibridge.pulse.domain.Model.MaintenanceTicket;
import com.medibridge.pulse.domain.Model.PlatformHealth;
import com.medibridge.pulse.domain.Model.TherapySession;
import com.medibridge.pulse.domain.Model.TherapyState;
import com.medibridge.pulse.domain.Model.Alarm;
import com.medibridge.pulse.domain.Model.AuditEvent;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PlatformService {

    private final Map<UUID, Device> devices = new ConcurrentHashMap<>();
    private final Map<UUID, DeviceAssignment> assignments = new ConcurrentHashMap<>();
    private final Map<UUID, InfusionOrder> infusionOrders = new ConcurrentHashMap<>();
    private final Map<UUID, TherapySession> therapies = new ConcurrentHashMap<>();
    private final Map<UUID, DrugLibrary> drugLibraries = new ConcurrentHashMap<>();
    private final Map<UUID, Alarm> alarms = new ConcurrentHashMap<>();
    private final Map<UUID, EmrDocument> emrDocs = new ConcurrentHashMap<>();
    private final Map<UUID, MaintenanceTicket> maintenanceTickets = new ConcurrentHashMap<>();
    private final Map<UUID, AuditEvent> auditEvents = new ConcurrentHashMap<>();
    private final List<DomainEvent> eventLog = new ArrayList<>();

    public List<Device> devices() { return devices.values().stream().toList(); }
    public List<DeviceAssignment> assignments() { return assignments.values().stream().toList(); }
    public List<InfusionOrder> orders() { return infusionOrders.values().stream().toList(); }
    public List<TherapySession> therapies() { return therapies.values().stream().toList(); }
    public List<DrugLibrary> libraries() { return drugLibraries.values().stream().toList(); }
    public List<Alarm> alarms() { return alarms.values().stream().toList(); }
    public List<EmrDocument> emrDocuments() { return emrDocs.values().stream().toList(); }
    public List<MaintenanceTicket> maintenanceTickets() { return maintenanceTickets.values().stream().toList(); }
    public List<AuditEvent> auditEvents() { return auditEvents.values().stream().toList(); }
    public List<DomainEvent> eventLog() { return List.copyOf(eventLog); }

    public Device registerDevice(String serial, String model, String firmware, String hospital, String ward, String bed) {
        Device device = new Device(UUID.randomUUID(), serial, model, firmware, hospital, ward, bed,
                DeviceStatus.REGISTERED, 100, 95, Instant.now());
        devices.put(device.id(), device);
        appendEvent("DeviceRegistered", "DeviceFleet", "DEVICE_REGISTERED", device.id().toString());
        return device;
    }

    public DeviceAssignment assignDevice(UUID deviceId, String patientId, String careUnit, String nurse) {
        Device current = devices.get(deviceId);
        Device updated = new Device(current.id(), current.serialNumber(), current.model(), current.firmwareVersion(),
                current.hospital(), current.ward(), current.bed(), DeviceStatus.ASSIGNED,
                current.batteryLevel(), current.wifiStrength(), Instant.now());
        devices.put(deviceId, updated);

        DeviceAssignment assignment = new DeviceAssignment(UUID.randomUUID(), deviceId, patientId, careUnit, nurse, Instant.now(), null);
        assignments.put(assignment.id(), assignment);
        appendEvent("DeviceAssignedToPatient", "DeviceFleet", "DEVICE_ASSIGNED_TO_PATIENT", assignment.id().toString());
        return assignment;
    }

    public InfusionOrder createInfusionOrder(String medication, String dose, String rate, String patientId, String physician, String approval) {
        InfusionOrder order = new InfusionOrder(UUID.randomUUID(), medication, dose, rate, patientId, physician, approval);
        infusionOrders.put(order.id(), order);
        appendEvent("InfusionOrderApproved", "PharmacyAdapter", "INFUSION_ORDER_ACCEPTED", order.id().toString());
        return order;
    }

    public TherapySession startTherapy(UUID orderId, UUID deviceId) {
        TherapySession session = new TherapySession(UUID.randomUUID(), orderId, deviceId, TherapyState.RUNNING,
                Instant.now(), null, 0.0, 500.0);
        therapies.put(session.id(), session);
        appendEvent("TherapyStarted", "TherapyManagement", "THERAPY_STARTED", session.id().toString());
        createEmrDocument(session.id(), "THERAPY_STARTED", "{" + "\"status\":\"started\"}");
        return session;
    }

    public TherapySession recordProgress(UUID sessionId, double infusedMl, double remainingMl) {
        TherapySession current = therapies.get(sessionId);
        TherapySession updated = new TherapySession(current.id(), current.orderId(), current.deviceId(), current.state(),
                current.startedAt(), current.endedAt(), infusedMl, remainingMl);
        therapies.put(sessionId, updated);
        appendEvent("InfusionProgressRecorded", "EdgeGateway", "INFUSION_PROGRESS_RECORDED", sessionId.toString());
        return updated;
    }

    public TherapySession completeTherapy(UUID sessionId) {
        TherapySession current = therapies.get(sessionId);
        TherapySession updated = new TherapySession(current.id(), current.orderId(), current.deviceId(), TherapyState.COMPLETED,
                current.startedAt(), Instant.now(), current.infusedVolumeMl(), 0.0);
        therapies.put(sessionId, updated);
        appendEvent("TherapyCompleted", "TherapyManagement", "THERAPY_COMPLETED", sessionId.toString());
        createEmrDocument(sessionId, "THERAPY_COMPLETED", "{" + "\"status\":\"completed\"}");
        return updated;
    }

    public DrugLibrary approveLibrary(String name, String version, String hospital, List<String> medications) {
        DrugLibrary library = new DrugLibrary(UUID.randomUUID(), name, version, hospital, "APPROVED", medications);
        drugLibraries.put(library.id(), library);
        appendEvent("DrugLibraryApproved", "DrugLibrary", "DRUG_LIBRARY_APPROVED", library.id().toString());
        return library;
    }

    public Alarm raiseAlarm(UUID deviceId, String patientId, AlarmSeverity severity, String category, String message) {
        Alarm alarm = new Alarm(UUID.randomUUID(), deviceId, patientId, severity, category, message, Instant.now(), null, null);
        alarms.put(alarm.id(), alarm);
        appendEvent("AlarmRaised", "EdgeGateway", "ALARM_RAISED", alarm.id().toString());
        createEmrDocument(null, "ALARM_RAISED", "{" + "\"alarm\":\"" + message + "\"}");
        return alarm;
    }

    public Alarm acknowledgeAlarm(UUID alarmId, String nurse) {
        Alarm current = alarms.get(alarmId);
        Alarm updated = new Alarm(current.id(), current.deviceId(), current.patientId(), current.severity(), current.category(),
                current.message(), current.raisedAt(), Instant.now(), nurse);
        alarms.put(alarmId, updated);
        appendEvent("AlarmAcknowledged", "AlarmManagement", "ALARM_ACKNOWLEDGED", alarmId.toString());
        return updated;
    }

    public MaintenanceTicket openMaintenanceTicket(UUID deviceId, String issueType, String priority, String assignee) {
        MaintenanceTicket ticket = new MaintenanceTicket(UUID.randomUUID(), deviceId, issueType, priority,
                MaintenanceStatus.OPEN, LocalDate.now().plusDays(5), assignee);
        maintenanceTickets.put(ticket.id(), ticket);
        appendEvent("TechnicalSafetyCheckDue", "Maintenance", "MAINTENANCE_TICKET_OPENED", ticket.id().toString());
        return ticket;
    }

    public EmrDocument createEmrDocument(UUID sessionId, String eventType, String payload) {
        EmrDocument doc = new EmrDocument(UUID.randomUUID(), sessionId, eventType, payload, "EXPORTED", 0, Instant.now());
        emrDocs.put(doc.id(), doc);
        appendEvent("DocumentationExported", "EmrIntegration", "DOCUMENTATION_EXPORTED", doc.id().toString());
        return doc;
    }

    public PlatformHealth platformHealth() {
        return new PlatformHealth(
                "UP",
                devices.size(),
                therapies.values().stream().filter(t -> t.state() == TherapyState.RUNNING).count(),
                alarms.values().stream().filter(a -> a.acknowledgedAt() == null).count(),
                maintenanceTickets.values().stream().filter(t -> t.status() != MaintenanceStatus.RESOLVED).count(),
                Instant.now()
        );
    }

    private void appendEvent(String type, String producer, String auditAction, String resource) {
        String correlation = UUID.randomUUID().toString();
        eventLog.add(new DomainEvent(UUID.randomUUID(), type, producer, correlation, Instant.now(), resource));
        AuditEvent audit = new AuditEvent(UUID.randomUUID(), producer, auditAction, resource, correlation, Instant.now());
        auditEvents.put(audit.id(), audit);
    }
}
