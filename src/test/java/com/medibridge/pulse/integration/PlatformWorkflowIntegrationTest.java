package com.medibridge.pulse.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medibridge.pulse.infra.persistence.DomainEventRepository;
import com.medibridge.pulse.infra.persistence.EmrDocumentRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.exactly;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Execution(ExecutionMode.SAME_THREAD)
class PlatformWorkflowIntegrationTest extends AbstractBaseIT {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    EmrDocumentRepository emrDocumentRepository;

    @Autowired
    DomainEventRepository domainEventRepository;

    @Test
    @Order(1)
    @DisplayName("should orchestrate therapy workflow and publish integration side effects")
    synchronized void therapyWorkflowPublishesKafkaAndExportsEmr() throws Exception {
        String unique = UUID.randomUUID().toString().substring(0, 8);

        MvcResult deviceResult = mockMvc.perform(post("/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "serialNumber": "IT-DEV-%s",
                                  "model": "PulseFlow X2",
                                  "firmwareVersion": "FW-9.9.9",
                                  "hospital": "St. Marien Klinikum",
                                  "ward": "ICU",
                                  "bed": "IT-1"
                                }
                                """.formatted(unique)))
                .andExpect(status().isOk())
                .andReturn();

        UUID deviceId = readUuid(deviceResult, "id");

        MvcResult orderResult = mockMvc.perform(post("/infusion-orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "medication": "Vancomycin",
                                  "dose": "500 mg",
                                  "rate": "2 mg/min",
                                  "patientId": "PAT-%s",
                                  "physician": "Dr. Integration",
                                  "pharmacyApproval": "PHARMACY_APPROVED"
                                }
                                """.formatted(unique)))
                .andExpect(status().isOk())
                .andReturn();

        UUID orderId = readUuid(orderResult, "id");

        mockMvc.perform(post("/therapy-sessions/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "orderId": "%s",
                                  "deviceId": "%s"
                                }
                                """.formatted(orderId, deviceId)))
                .andExpect(status().isOk());

        assertThat(emrDocumentRepository.count()).isGreaterThan(0);
        assertThat(domainEventRepository.count()).isGreaterThan(0);

        WIREMOCK.verify(exactly(1), postRequestedFor(urlEqualTo("/documents")));

        List<?> kafkaEvents = readKafkaRecords("medibridge.domain-events", 1);
        assertThat(kafkaEvents).isNotEmpty();
    }

    @RepeatedTest(2)
    @Order(2)
    @DisplayName("repeated isolated data setup should avoid inter-test collisions")
    synchronized void repeatedIsolatedDataShouldStayStable() throws Exception {
        String unique = UUID.randomUUID().toString().substring(0, 8);
        mockMvc.perform(post("/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "serialNumber": "IT-ISO-%s",
                                  "model": "PulseFlow Neo",
                                  "firmwareVersion": "FW-1.0.1",
                                  "hospital": "Nordstadt Medical Center",
                                  "ward": "Oncology",
                                  "bed": "ISO-2"
                                }
                                """.formatted(unique)))
                .andExpect(status().isOk());

        MvcResult devicesResult = mockMvc.perform(get("/devices"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode array = objectMapper.readTree(devicesResult.getResponse().getContentAsString());
        assertThat(array.isArray()).isTrue();
        assertThat(array.toString()).contains("IT-ISO-");
    }

    @Test
    @Disabled("Example only: keep for course notes when demonstrating selective disabling.")
    void disabledExample() {
    }

    private UUID readUuid(MvcResult result, String field) throws Exception {
        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        return UUID.fromString(node.get(field).asText());
    }
}
