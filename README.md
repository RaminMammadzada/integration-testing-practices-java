# MediBridge Pulse Platform

A cloud-native Spring Boot reference platform for connected hospital infusion-device integration, event-driven clinical workflows, EMR interoperability, and regulated healthcare traceability.

## 1) Problem statement

Modern hospitals run large fleets of connected infusion devices across ICUs, oncology wards, surgery units, emergency departments, pediatrics, and renal care units. Clinical teams and biomedical engineering teams depend on interoperable software that can coordinate telemetry, alarms, infusion workflow data, and documentation exports safely.

This project models a secure integration hub that coordinates:

- Infusion device fleet operations
- Infusion order and therapy-session workflows
- Drug-library safety deployment
- Alarm routing and acknowledgements
- EMR documentation exports
- Maintenance workflows and compliance-grade audit traces

## 2) Architecture at a glance

`MediBridge Pulse Platform` is implemented as a modular Spring Boot application with workflow-oriented APIs.

### Bounded-context inspired modules

- **Device Fleet Management**: device registration, assignment, status and heartbeat modeling
- **Infusion Therapy Management**: infusion orders, therapy sessions, progress updates
- **Drug Library Safety**: library approval and deployment references
- **Clinical Documentation Integration**: generated EMR documentation events
- **Alarm & Escalation Management**: raise/acknowledge alarms with severity
- **Biomedical Maintenance**: maintenance ticket creation
- **Compliance & Audit**: immutable audit event timeline + domain event log

### Event-driven style

Each workflow operation appends:

1. A **domain event** (integration/event-log view)
2. A corresponding **audit event** (traceability/compliance view)

This creates a simple event backbone suitable for later replacement with Kafka or another broker.

## 3) Step-by-step implementation plan used in this repository

1. **Scaffold platform**
   - Spring Boot setup
   - Java 21 baseline
   - REST + Validation + Actuator
2. **Build domain model**
   - Device, Assignment, InfusionOrder, TherapySession, DrugLibrary, Alarm, EmrDocument, MaintenanceTicket, AuditEvent, DomainEvent
3. **Implement workflow service layer**
   - In-memory platform service
   - Event and audit append logic
4. **Expose workflow-oriented APIs**
   - `/devices`, `/device-assignments`, `/infusion-orders`, `/therapy-sessions`, `/drug-libraries`, `/alarms`, `/emr-documents`, `/maintenance-tickets`, `/audit-events`, `/platform-health`
5. **Seed realistic hospital world data**
   - Multiple hospitals/wards/roles/medications
6. **Containerize and document**
   - Dockerfile + docker-compose
   - API examples

## 4) API resources

- `GET/POST /devices`
- `GET/POST /device-assignments`
- `GET/POST /infusion-orders`
- `GET /therapy-sessions`
- `POST /therapy-sessions/start`
- `POST /therapy-sessions/{id}/progress`
- `POST /therapy-sessions/{id}/complete`
- `GET/POST /drug-libraries`
- `GET/POST /alarms`
- `POST /alarms/{id}/ack`
- `GET /emr-documents`
- `GET/POST /maintenance-tickets`
- `GET /audit-events`
- `GET /events`
- `GET /platform-health`

## 5) Quick start

### Local Java run

```bash
./mvnw spring-boot:run
```

or

```bash
mvn spring-boot:run
```

### Docker Compose run

```bash
docker compose up --build
```

App URL: `http://localhost:8080`

## 6) Example calls

Register device:

```bash
curl -X POST http://localhost:8080/devices \
  -H 'Content-Type: application/json' \
  -d '{
    "serialNumber":"MB-ICU-4040",
    "model":"PulseFlow X2",
    "firmwareVersion":"FW-3.5.0",
    "hospital":"Rhein-Neckar Care Hospital",
    "ward":"ICU",
    "bed":"ICU-08"
  }'
```

Create infusion order:

```bash
curl -X POST http://localhost:8080/infusion-orders \
  -H 'Content-Type: application/json' \
  -d '{
    "medication":"Paracetamol IV",
    "dose":"1000 mg",
    "rate":"5 mg/min",
    "patientId":"PAT-88120",
    "physician":"Dr. A. Lorenz",
    "pharmacyApproval":"PHARMACY_APPROVED"
  }'
```

Inspect platform health:

```bash
curl http://localhost:8080/platform-health
```

## 7) Future enhancements

- Replace in-memory store with PostgreSQL + outbox table
- Introduce message broker integration (Kafka/RabbitMQ)
- Add idempotency keys and distributed tracing headers
- Add OAuth2/JWT security and role-based authorization
- Provide OpenAPI contracts and contract tests
- Add realistic external-system simulators (EMR, pharmacy, maintenance vendor)
