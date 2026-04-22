package com.medibridge.pulse.seed;

import com.medibridge.pulse.domain.Model;
import com.medibridge.pulse.service.PlatformService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SampleDataInitializer {

    @Bean
    CommandLineRunner seedPlatformData(PlatformService service) {
        return args -> {
            Model.Device d1 = service.registerDevice("MB-ICU-1001", "PulseFlow X2", "FW-3.4.2", "St. Marien Klinikum", "ICU", "ICU-12");
            Model.Device d2 = service.registerDevice("MB-ONC-2209", "PulseFlow X2", "FW-3.3.9", "Nordstadt Medical Center", "Oncology", "ONC-07");
            service.registerDevice("MB-SUR-3372", "PulseFlow Neo", "FW-2.9.1", "Alpine Surgical Center", "Surgery", "SUR-03");

            service.assignDevice(d1.id(), "PAT-44812", "ICU", "Nurse Lena Brandt");
            service.assignDevice(d2.id(), "PAT-99104", "Oncology", "Nurse Fatima Kaya");

            Model.InfusionOrder order = service.createInfusionOrder(
                    "Vancomycin", "1000 mg", "4 mg/min", "PAT-44812", "Dr. M. Weber", "PHARMACY_APPROVED");

            Model.TherapySession session = service.startTherapy(order.id(), d1.id());
            service.recordProgress(session.id(), 120.5, 379.5);

            service.approveLibrary("ICU Core Library", "ICU-LIB-2026.04", "St. Marien Klinikum",
                    List.of("Saline 0.9%", "Vancomycin", "Heparin", "Norepinephrine", "Insulin"));

            Model.Alarm alarm = service.raiseAlarm(d1.id(), "PAT-44812", Model.AlarmSeverity.HIGH,
                    "OCCLUSION_DETECTED", "Occlusion detected in channel A");
            service.acknowledgeAlarm(alarm.id(), "Nurse Lena Brandt");

            service.openMaintenanceTicket(d2.id(), "Firmware version outdated", "MEDIUM", "Engineer Tobias Lang");
        };
    }
}
