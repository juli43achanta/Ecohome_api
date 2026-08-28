package com.ecohome.api.dto;

import com.ecohome.api.model.Dispositivo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class DispositivoDTO {
    private Integer id;
    @NotBlank
    private String nombre;
    @NotNull
    private Dispositivo.Tipo tipo;
    private Boolean estado;
    private Float valorActual;
    private String ipUrl;
    private String mqttTopic;
    private LocalDateTime ultimaModificacion;
    private Integer usuarioId;

    public static DispositivoDTO from(Dispositivo d) {
        DispositivoDTO dto = new DispositivoDTO();
        dto.id = d.getId();
        dto.nombre = d.getNombre();
        dto.tipo = d.getTipo();
        dto.estado = d.getEstado();
        dto.valorActual = d.getValorActual();
        dto.ipUrl = d.getIpUrl();
        dto.mqttTopic = d.getMqttTopic();
        dto.ultimaModificacion = d.getUltimaModificacion();
        dto.usuarioId = d.getUsuario().getId();
        return dto;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public Dispositivo.Tipo getTipo() { return tipo; }
    public void setTipo(Dispositivo.Tipo tipo) { this.tipo = tipo; }
    public Boolean getEstado() { return estado; }
    public void setEstado(Boolean estado) { this.estado = estado; }
    public Float getValorActual() { return valorActual; }
    public void setValorActual(Float valorActual) { this.valorActual = valorActual; }
    public String getIpUrl() { return ipUrl; }
    public void setIpUrl(String ipUrl) { this.ipUrl = ipUrl; }
    public String getMqttTopic() { return mqttTopic; }
    public void setMqttTopic(String mqttTopic) { this.mqttTopic = mqttTopic; }
    public LocalDateTime getUltimaModificacion() { return ultimaModificacion; }
    public void setUltimaModificacion(LocalDateTime ultimaModificacion) { this.ultimaModificacion = ultimaModificacion; }
    public Integer getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Integer usuarioId) { this.usuarioId = usuarioId; }
}
