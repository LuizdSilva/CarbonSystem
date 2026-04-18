package com.carbontreesystem.config;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class MqttConfig {

    @Value("${mqtt.broker.url:tcp://localhost:1883}")
    private String brokerUrl;

    @Value("${mqtt.client.id:carbontree-server}")
    private String clientId;

    @Bean
    public MqttClient mqttClient() {
        try {
            //--Cria o cliente que vai se conectar ao servidor de mensagens dos sensores--
            MqttClient client = new MqttClient(brokerUrl, clientId + "-" + System.currentTimeMillis(),
                    new MemoryPersistence());

            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            options.setConnectionTimeout(10);
            options.setKeepAliveInterval(60);
            options.setAutomaticReconnect(true);

            log.info("Tentando conexão MQTT em: {}", brokerUrl);
            client.connect(options);
            log.info("MQTT conectado com sucesso!");
            return client;
        } catch (MqttException e) {
            log.warn("Broker MQTT não disponível ({}). O sistema funcionará sem sensores em tempo real. Erro: {}",
                    brokerUrl, e.getMessage());
            return null;
        }
    }
}