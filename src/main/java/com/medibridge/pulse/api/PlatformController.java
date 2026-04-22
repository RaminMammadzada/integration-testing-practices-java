package com.medibridge.pulse.api;

import com.medibridge.pulse.domain.Model;
import com.medibridge.pulse.service.PlatformService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping
public class PlatformController {

    private final PlatformService service;

    public PlatformController(PlatformService service) {
        this.service = service;
    }

    @GetMapping("/devices")
    public List<Model.Device> devices() { return service.devices(); }

    @PostMapping("/devices")
    public Model.Device registerDevice(@RequestBody @Valid RegisterDeviceRequest request) {
        return service.registerDevice(request.serialNumber(), request.model(), request.firmwareVersion(),
                request.hospital(), request.ward(), request.bed());
    }

    @PostMapping("/device-assignments")
    public Model.DeviceAssignment assignDevice(@RequestBody @Valid AssignDeviceRequest request) {
        return service.assignDevice(request.deviceId(), request.patientId(), request.careUnit(), request.nurse());
    }

    @GetMapping("/device-assignments")
    public List<Model.DeviceAssignment> assignments() { return service.assignments(); }

    @PostMapping("/infusion-orders")
    public Model.InfusionOrder createOrder(@RequestBody @Valid CreateInfusionOrderRequest request) {
        return service.createInfusionOrder(request.medication(), request.dose(), request.rate(),
                request.patientId(), request.physician(), request.pharmacyApproval());
    }

    @GetMapping("/infusion-orders")
    public List<Model.InfusionOrder> orders() { return service.orders(); }

    @PostMapping("/therapy-sessions/start")
    public Model.TherapySession startTherapy(@RequestBody @Valid StartTherapyRequest request) {
        return service.startTherapy(request.orderId(), request.deviceId());
    }

    @PostMapping("/therapy-sessions/{sessionId}/progress")
    public Model.TherapySession progress(@PathVariable UUID sessionId, @RequestBody @Valid RecordProgressRequest request) {
        return service.recordProgress(sessionId, request.infusedVolumeMl(), request.remainingVolumeMl());
    }

    @PostMapping("/therapy-sessions/{sessionId}/complete")
    public Model.TherapySession complete(@PathVariable UUID sessionId) {
        return service.completeTherapy(sessionId);
    }

    @GetMapping("/therapy-sessions")
    public List<Model.TherapySession> therapies() { return service.therapies(); }

    @PostMapping("/drug-libraries")
    public Model.DrugLibrary approveLibrary(@RequestBody @Valid ApproveDrugLibraryRequest request) {
        return service.approveLibrary(request.libraryName(), request.version(), request.hospital(), request.medications());
    }

    @GetMapping("/drug-libraries")
    public List<Model.DrugLibrary> libraries() { return service.libraries(); }

    @PostMapping("/alarms")
    public Model.Alarm raiseAlarm(@RequestBody @Valid RaiseAlarmRequest request) {
        return service.raiseAlarm(request.deviceId(), request.patientId(), request.severity(), request.category(), request.message());
    }

    @PostMapping("/alarms/{alarmId}/ack")
    public Model.Alarm acknowledge(@PathVariable UUID alarmId, @RequestBody @Valid AcknowledgeAlarmRequest request) {
        return service.acknowledgeAlarm(alarmId, request.nurse());
    }

    @GetMapping("/alarms")
    public List<Model.Alarm> alarms() { return service.alarms(); }

    @GetMapping("/emr-documents")
    public List<Model.EmrDocument> emrDocuments() { return service.emrDocuments(); }

    @PostMapping("/maintenance-tickets")
    public Model.MaintenanceTicket openTicket(@RequestBody @Valid OpenMaintenanceTicketRequest request) {
        return service.openMaintenanceTicket(request.deviceId(), request.issueType(), request.priority(), request.assignee());
    }

    @GetMapping("/maintenance-tickets")
    public List<Model.MaintenanceTicket> tickets() { return service.maintenanceTickets(); }

    @GetMapping("/audit-events")
    public List<Model.AuditEvent> auditEvents() { return service.auditEvents(); }

    @GetMapping("/events")
    public List<Model.DomainEvent> events() { return service.eventLog(); }

    @GetMapping("/platform-health")
    public Model.PlatformHealth health() { return service.platformHealth(); }

    public record RegisterDeviceRequest(@NotBlank String serialNumber, @NotBlank String model, @NotBlank String firmwareVersion,
                                        @NotBlank String hospital, @NotBlank String ward, @NotBlank String bed) {}

    public record AssignDeviceRequest(@NotNull UUID deviceId, @NotBlank String patientId, @NotBlank String careUnit, @NotBlank String nurse) {}

    public record CreateInfusionOrderRequest(@NotBlank String medication, @NotBlank String dose, @NotBlank String rate,
                                             @NotBlank String patientId, @NotBlank String physician, @NotBlank String pharmacyApproval) {}

    public record StartTherapyRequest(@NotNull UUID orderId, @NotNull UUID deviceId) {}

    public record RecordProgressRequest(double infusedVolumeMl, double remainingVolumeMl) {}

    public record ApproveDrugLibraryRequest(@NotBlank String libraryName, @NotBlank String version,
                                            @NotBlank String hospital, @NotNull List<String> medications) {}

    public record RaiseAlarmRequest(@NotNull UUID deviceId, @NotBlank String patientId,
                                    @NotNull Model.AlarmSeverity severity, @NotBlank String category, @NotBlank String message) {}

    public record AcknowledgeAlarmRequest(@NotBlank String nurse) {}

    public record OpenMaintenanceTicketRequest(@NotNull UUID deviceId, @NotBlank String issueType,
                                               @NotBlank String priority, @NotBlank String assignee) {}
}
