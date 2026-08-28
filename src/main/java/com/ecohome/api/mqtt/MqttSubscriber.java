package com.ecohome.api.mqtt;

import com.ecohome.api.model.Dispositivo;
import com.ecohome.api.model.HistorialActividad;
import com.ecohome.api.repository.DispositivoRepository;
import com.ecohome.api.repository.HistorialRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.paho.client.mqttv3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class MqttSubscriber implements MqttCallback {

    private static final Logger log = LoggerFactory.getLogger(MqttSubscriber.class);

    private final DispositivoRepository dispositivoRepository;
    private final HistorialRepository historialRepository;
    private final SimpMessagingTemplate websocket;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MqttSubscriber(DispositivoRepository dispositivoRepository,
                           HistorialRepository historialRepository,
                           SimpMessagingTemplate websocket) {
        this.dispositivoRepository = dispositivoRepository;
        this.historialRepository = historialRepository;
        this.websocket = websocket;
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        String payload = new String(message.getPayload());
        log.debug("MQTT recibido [{}]: {}", topic, payload);

        try {
            if (topic.startsWith("ecohome/dispositivo/")) {
                procesarMensajeEcoHome(topic, payload);
                return;
            }
            if (topic.startsWith("stat/") && topic.endsWith("/POWER")) {
                procesarTasmotaPower(topic, payload);
                return;
            }
            if (topic.startsWith("tele/") && topic.endsWith("/SENSOR")) {
                procesarTasmotaSensor(topic, payload);
                return;
            }
            if (topic.startsWith("shellies/") && topic.contains("/relay/")) {
                procesarShelly(topic, payload);
            }
        } catch (Exception e) {
            log.error("Error procesando mensaje MQTT topic={}: {}", topic, e.getMessage());
        }
    }

    private void procesarMensajeEcoHome(String topic, String payload) throws Exception {
        // topic: ecohome/dispositivo/{id}/estado
        String[] parts = topic.split("/");
        if (parts.length < 3) return;
        int id = Integer.parseInt(parts[2]);

        Optional<Dispositivo> opt = dispositivoRepository.findById(id);
        if (opt.isEmpty()) return;

        Dispositivo dispositivo = opt.get();
        JsonNode json = objectMapper.readTree(payload);

        if (json.has("estado")) {
            dispositivo.setEstado(json.get("estado").asBoolean());
        }
        if (json.has("valor")) {
            dispositivo.setValorActual((float) json.get("valor").asDouble());
        }
        dispositivoRepository.save(dispositivo);
        registrarHistorial(dispositivo, payload);
        notificarClientes(dispositivo);
    }

    private void procesarTasmotaPower(String topic, String payload) throws Exception {
        Optional<Dispositivo> opt = dispositivoRepository.findByMqttTopic(topic);
        if (opt.isEmpty()) return;
        Dispositivo dispositivo = opt.get();
        JsonNode json = objectMapper.readTree(payload);
        String power = json.has("POWER") ? json.get("POWER").asText() : payload;
        dispositivo.setEstado("ON".equalsIgnoreCase(power));
        dispositivoRepository.save(dispositivo);
        registrarHistorial(dispositivo, "Tasmota POWER: " + power);
        notificarClientes(dispositivo);
    }

    private void procesarTasmotaSensor(String topic, String payload) throws Exception {
        Optional<Dispositivo> opt = dispositivoRepository.findByMqttTopic(topic);
        if (opt.isEmpty()) return;
        Dispositivo dispositivo = opt.get();
        JsonNode json = objectMapper.readTree(payload);
        float temp = 0;
        if (json.has("DS18B20")) temp = (float) json.get("DS18B20").get("Temperature").asDouble();
        else if (json.has("AM2301")) temp = (float) json.get("AM2301").get("Temperature").asDouble();
        else if (json.has("SHT3X")) temp = (float) json.get("SHT3X").get("Temperature").asDouble();
        if (temp != 0) {
            dispositivo.setValorActual(temp);
            dispositivoRepository.save(dispositivo);
            notificarClientes(dispositivo);
        }
    }

    private void procesarShelly(String topic, String payload) {
        Optional<Dispositivo> opt = dispositivoRepository.findByMqttTopic(topic);
        if (opt.isEmpty()) return;
        Dispositivo dispositivo = opt.get();
        dispositivo.setEstado("on".equalsIgnoreCase(payload.trim()));
        dispositivoRepository.save(dispositivo);
        registrarHistorial(dispositivo, "Shelly: " + payload);
        notificarClientes(dispositivo);
    }

    private void registrarHistorial(Dispositivo d, String descripcion) {
        HistorialActividad h = new HistorialActividad();
        h.setUsuario(d.getUsuario());
        h.setDispositivoNombre(d.getNombre());
        h.setTipo(d.getTipo().name());
        h.setDescripcion("[IoT] " + descripcion);
        h.setFechaHora(LocalDateTime.now());
        historialRepository.save(h);
    }

    private void notificarClientes(Dispositivo d) {
        websocket.convertAndSend("/topic/dispositivos/" + d.getId(), d);
    }

    @Override
    public void connectionLost(Throwable cause) {
        log.warn("Conexion MQTT perdida: {}. Reconectando...", cause.getMessage());
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // no-op for subscriber
    }
}
