package com.carbontreesystem.service;

import com.carbontreesystem.dto.MqttPayloadDto;
import com.carbontreesystem.model.SensorReading;
import com.carbontreesystem.model.Station;
import com.carbontreesystem.repository.SensorReadingRepository;
import com.carbontreesystem.repository.StationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
public class MqttService {

    private final StationRepository stationRepo;
    private final SensorReadingRepository readingRepo;
    private final AlertService alertService;
    private final ObjectMapper objectMapper;


    private final MqttClient mqttClient;

    @Value("${mqtt.topic.sensors:sensors/#}")
    private String sensorTopic;

    @Autowired
    public MqttService(StationRepository stationRepo,
                       SensorReadingRepository readingRepo,
                       AlertService alertService,
                       ObjectMapper objectMapper,
                       @Autowired(required = false) MqttClient mqttClient) {
        this.stationRepo   = stationRepo;
        this.readingRepo   = readingRepo;
        this.alertService  = alertService;
        this.objectMapper  = objectMapper;
        this.mqttClient    = mqttClient;
    }

    @PostConstruct
    public void subscribeToSensors() {
        if (mqttClient == null || !mqttClient.isConnected()) {
            log.warn("MQTT client indisponível — subscription ignorada. App funciona normalmente.");
            return;
        }
        try {
            mqttClient.subscribe(sensorTopic, 1, this::handleMessage);
            log.info("Inscrito no tópico MQTT: {}", sensorTopic);
        } catch (MqttException e) {
            log.error("Falha ao inscrever no tópico MQTT: {}", e.getMessage());
        }
    }

    private void handleMessage(String topic, MqttMessage message) {
        try {
            String payload = new String(message.getPayload());
            log.debug("MQTT mensagem em [{}]: {}", topic, payload);
            MqttPayloadDto dto = objectMapper.readValue(payload, MqttPayloadDto.class);
            processPayload(dto);
        } catch (Exception e) {
            log.error("Erro ao processar mensagem MQTT: {}", e.getMessage());
        }
    }

    @Transactional
    public void processPayload(MqttPayloadDto dto) {

        Station station = stationRepo.findByStationCode(dto.getStationCode())
                .orElseGet(() -> {
                    log.info("Auto-registrando nova estação: {}", dto.getStationCode());
                    return stationRepo.save(Station.builder()
                            .stationCode(dto.getStationCode())   // CORRIGIDO L67
                            .name("Station " + dto.getStationCode())  // CORRIGIDO L68
                            .status(Station.StationStatus.ONLINE)
                            .lastSeen(LocalDateTime.now())
                            .build());
                });

        station.setStatus(Station.StationStatus.ONLINE);
        station.setLastSeen(LocalDateTime.now());
        stationRepo.save(station);

        SensorReading reading = SensorReading.builder()
                .station(station)
                .co2Level(dto.getCo2Level())      // CORRIGIDO L83
                .pmLevel(dto.getPmLevel())         // CORRIGIDO L84
                .temperature(dto.getTemperature()) // L85 — correto
                .humidity(dto.getHumidity())       // L86 — correto
                .recordedAt(LocalDateTime.now())
                .source(SensorReading.ReadingSource.MQTT)
                .build();
        readingRepo.save(reading);

        alertService.evaluateReading(reading);
    }
}
