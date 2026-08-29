package com.ecohome.api.mqtt;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "ecohome.mqtt.enabled", havingValue = "true")
public class MqttPublisher {

    private static final Logger log = LoggerFactory.getLogger(MqttPublisher.class);

    private final MqttClient mqttClient;

    @Autowired
    public MqttPublisher(MqttClient mqttClient) {
        this.mqttClient = mqttClient;
    }

    public void encender(String commandTopic) {
        publicar(commandTopic, "ON");
    }

    public void apagar(String commandTopic) {
        publicar(commandTopic, "OFF");
    }

    public void publicar(String topic, String payload) {
        if (mqttClient == null || !mqttClient.isConnected()) {
            log.warn("MQTT no disponible — comando ignorado [{}]", topic);
            return;
        }
        try {
            MqttMessage msg = new MqttMessage(payload.getBytes());
            msg.setQos(1);
            msg.setRetained(true);
            mqttClient.publish(topic, msg);
            log.debug("MQTT publicado [{}]: {}", topic, payload);
        } catch (Exception e) {
            log.error("Error publicando MQTT topic={}: {}", topic, e.getMessage());
        }
    }

    /** Convierte topic de estado al topic de comando Tasmota: stat/DEVICE/POWER → cmnd/DEVICE/POWER */
    public String toCommandTopic(String statTopic) {
        return statTopic.replace("stat/", "cmnd/").replace("tele/", "cmnd/");
    }
}
