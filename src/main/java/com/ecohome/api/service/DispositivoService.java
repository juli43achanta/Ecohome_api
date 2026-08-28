package com.ecohome.api.service;

import com.ecohome.api.dto.DispositivoDTO;
import com.ecohome.api.model.Dispositivo;
import com.ecohome.api.model.HistorialActividad;
import com.ecohome.api.model.Usuario;
import com.ecohome.api.mqtt.MqttPublisher;
import com.ecohome.api.repository.DispositivoRepository;
import com.ecohome.api.repository.HistorialRepository;
import com.ecohome.api.repository.UsuarioRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DispositivoService {

    private final DispositivoRepository dispositivoRepository;
    private final UsuarioRepository usuarioRepository;
    private final HistorialRepository historialRepository;
    private final MqttPublisher mqttPublisher;
    private final SimpMessagingTemplate websocket;

    public DispositivoService(DispositivoRepository dispositivoRepository,
                               UsuarioRepository usuarioRepository,
                               HistorialRepository historialRepository,
                               MqttPublisher mqttPublisher,
                               SimpMessagingTemplate websocket) {
        this.dispositivoRepository = dispositivoRepository;
        this.usuarioRepository = usuarioRepository;
        this.historialRepository = historialRepository;
        this.mqttPublisher = mqttPublisher;
        this.websocket = websocket;
    }

    public List<DispositivoDTO> listarPorUsuario(Integer usuarioId) {
        return dispositivoRepository.findByUsuarioId(usuarioId)
                .stream().map(DispositivoDTO::from).toList();
    }

    public DispositivoDTO crear(DispositivoDTO dto, Integer usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId).orElseThrow();
        Dispositivo d = new Dispositivo();
        d.setNombre(dto.getNombre());
        d.setTipo(dto.getTipo());
        d.setEstado(Boolean.TRUE.equals(dto.getEstado()));
        d.setValorActual(dto.getValorActual() != null ? dto.getValorActual() : 20.0f);
        d.setIpUrl(dto.getIpUrl());
        d.setMqttTopic(dto.getMqttTopic());
        d.setUsuario(usuario);
        return DispositivoDTO.from(dispositivoRepository.save(d));
    }

    public DispositivoDTO modificar(Integer id, DispositivoDTO dto) {
        Dispositivo d = dispositivoRepository.findById(id).orElseThrow();
        d.setNombre(dto.getNombre());
        d.setTipo(dto.getTipo());
        d.setIpUrl(dto.getIpUrl());
        d.setMqttTopic(dto.getMqttTopic());
        d.setValorActual(dto.getValorActual());
        return DispositivoDTO.from(dispositivoRepository.save(d));
    }

    public void eliminar(Integer id) {
        dispositivoRepository.deleteById(id);
    }

    public DispositivoDTO toggleEstado(Integer id) {
        Dispositivo d = dispositivoRepository.findById(id).orElseThrow();
        d.setEstado(!d.getEstado());
        dispositivoRepository.save(d);

        if (d.getMqttTopic() != null && !d.getMqttTopic().isBlank()) {
            String cmdTopic = mqttPublisher.toCommandTopic(d.getMqttTopic());
            if (d.getEstado()) mqttPublisher.encender(cmdTopic);
            else mqttPublisher.apagar(cmdTopic);
        }

        registrarHistorial(d, d.getEstado() ? "Encendido" : "Apagado");
        DispositivoDTO resultado = DispositivoDTO.from(d);
        websocket.convertAndSend("/topic/dispositivos/" + d.getId(), resultado);
        return resultado;
    }

    public DispositivoDTO setValor(Integer id, float valor) {
        Dispositivo d = dispositivoRepository.findById(id).orElseThrow();
        d.setValorActual(valor);
        dispositivoRepository.save(d);

        if (d.getMqttTopic() != null && !d.getMqttTopic().isBlank()) {
            String cmdTopic = mqttPublisher.toCommandTopic(d.getMqttTopic());
            mqttPublisher.publicar(cmdTopic, String.format("{\"setpoint\":%.1f}", valor));
        }

        registrarHistorial(d, "Valor actualizado: " + valor);
        DispositivoDTO resultado = DispositivoDTO.from(d);
        websocket.convertAndSend("/topic/dispositivos/" + d.getId(), resultado);
        return resultado;
    }

    private void registrarHistorial(Dispositivo d, String descripcion) {
        HistorialActividad h = new HistorialActividad();
        h.setUsuario(d.getUsuario());
        h.setDispositivoNombre(d.getNombre());
        h.setTipo(d.getTipo().name());
        h.setDescripcion(descripcion);
        h.setFechaHora(LocalDateTime.now());
        historialRepository.save(h);
    }
}
