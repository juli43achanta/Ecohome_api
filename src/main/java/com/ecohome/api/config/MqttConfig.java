package com.ecohome.api.config;

import com.ecohome.api.mqtt.MqttSubscriber;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLSocketFactory;

@Configuration
public class MqttConfig {

    private static final Logger log = LoggerFactory.getLogger(MqttConfig.class);

    @Value("${ecohome.mqtt.broker}")
    private String broker;

    @Value("${ecohome.mqtt.client-id}")
    private String clientId;

    @Value("${ecohome.mqtt.username}")
    private String username;

    @Value("${ecohome.mqtt.password}")
    private String password;

    @Bean
    @ConditionalOnProperty(name = "ecohome.mqtt.enabled", havingValue = "true", matchIfMissing = true)
    public MqttClient mqttClient(MqttSubscriber subscriber) {
        try {
            MqttClient client = new MqttClient(broker, clientId, new MemoryPersistence());

            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            options.setAutomaticReconnect(true);
            options.setConnectionTimeout(5);

            // TLS: si el broker usa ssl:// se configura SSLSocketFactory automáticamente
            // Para brokers con certificado válido (HiveMQ Cloud, Let's Encrypt) esto es suficiente.
            // Para certificados autofirmados configurar un TrustManager personalizado.
            if (broker.startsWith("ssl://")) {
                options.setSocketFactory(SSLSocketFactory.getDefault());
                log.info("MQTT con TLS habilitado para {}", broker);
            }

            if (username != null && !username.isBlank()) {
                options.setUserName(username);
                options.setPassword(password.toCharArray());
            }

            // Advertencia de seguridad: TCP sin cifrado hacia host remoto
            if (broker.startsWith("tcp://") && !broker.contains("localhost") && !broker.contains("127.0.0.1")) {
                log.warn("⚠️  MQTT usando TCP sin cifrar hacia host remoto ({}). " +
                         "Configura MQTT_BROKER=ssl://broker:8883 y credenciales en producción.", broker);
            }
            if (!broker.startsWith("ssl://") && (username == null || username.isBlank())) {
                log.warn("⚠️  MQTT sin credenciales. Configura MQTT_USER y MQTT_PASSWORD para el broker.");
            }

            client.setCallback(subscriber);
            client.connect(options);
            client.subscribe("ecohome/#", 1);

            log.info("MQTT conectado a {} y suscrito a ecohome/#", broker);
            return client;

        } catch (MqttException e) {
            log.warn("No se pudo conectar al broker MQTT ({}): {}. " +
                     "El sistema funciona sin IoT en tiempo real. " +
                     "Activa Mosquitto o HiveMQ Cloud para habilitar la integracion hardware.",
                     broker, e.getMessage());
            // Devolver cliente sin conectar — MqttPublisher comprueba isConnected() antes de publicar
            try {
                return new MqttClient(broker, clientId + "-offline", new MemoryPersistence());
            } catch (MqttException ex) {
                log.error("No se pudo crear MqttClient offline: {}", ex.getMessage());
                return null;
            }
        }
    }
}
