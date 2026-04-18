package com.carbontreesystem.config;

import com.carbontreesystem.model.*;
import com.carbontreesystem.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Random;


@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner initData(
            UserRepository userRepo,
            StationRepository stationRepo,
            SensorReadingRepository readingRepo,
            AlertRepository alertRepo,
            CarbonCreditRepository creditRepo,
            ConformityParameterRepository conformityRepo
    ) {
        return args -> {
            log.info("Iniciando a criação de dados de demonstração...");

            //-- Usuários--
            if (!userRepo.existsByUsername("admin")) {
                userRepo.save(User.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("admin123"))
                        .email("admin@carbontree.com")
                        .fullName("Administrador")
                        .role(User.Role.ADMIN)
                        .build());
            }

            //-- Parâmetros de Conformidade--
            if (conformityRepo.count() == 0) {
                conformityRepo.save(ConformityParameter.builder()
                        .parameterName("Decreto nº 11.550/2023 - CO2")
                        .maxCo2Level(1000.0)
                        .maxPmLevel(150.0)
                        .legalReference("Decreto nº 11.550/2023")
                        .description("Limites legais para emissão de CO2")
                        .active(true)
                        .build());
            }

            //--Estações (STATIONS)--
            if (stationRepo.count() == 0) {
                Station s1 = stationRepo.save(Station.builder()
                        .stationCode("ST-001")
                        .name("Estação Norte")
                        .location("Setor Norte - Facens")
                        .status(Station.StationStatus.ONLINE)
                        .latitude(-23.5000)
                        .longitude(-47.4500)
                        .lastSeen(LocalDateTime.now())
                        .build());

                // === Leituras Simuladas ===
                Random rng = new Random();
                LocalDateTime now = LocalDateTime.now();
                for (int h = 23; h >= 0; h--) {
                    readingRepo.save(SensorReading.builder()
                            .station(s1)
                            .co2Level(400 + rng.nextDouble() * 400)
                            .pmLevel(20 + rng.nextDouble() * 30)
                            .temperature(20 + rng.nextDouble() * 10)
                            .humidity(50 + rng.nextDouble() * 30)
                            .recordedAt(now.minusHours(h))
                            .source(SensorReading.ReadingSource.SIMULATED)
                            .build());
                }
            }

            log.info("Dados de demonstração criados com sucesso!");
        };
    }
}