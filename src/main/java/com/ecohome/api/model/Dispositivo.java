package com.ecohome.api.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "dispositivos")
public class Dispositivo {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Tipo tipo;

    @Column(nullable = false)
    private Boolean estado = false;

    @Column(name = "valor_actual")
    private Float valorActual = 20.0f;

    @Column(name = "ip_url", length = 255)
    private String ipUrl;

    @Column(name = "mqtt_topic", length = 255)
    private String mqttTopic;

    @Column(name = "ultima_modificacion")
    private LocalDateTime ultimaModificacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @PrePersist @PreUpdate
    private void actualizarTimestamp() { ultimaModificacion = LocalDateTime.now(); }

    public enum Tipo { CALEFACCION, CAMARA, LUZ }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public Tipo getTipo() { return tipo; }
    public void setTipo(Tipo tipo) { this.tipo = tipo; }
    public Boolean getEstado() { return estado; }
    public void setEstado(Boolean estado) { this.estado = estado; }
    public Float getValorActual() { return valorActual; }
    public void setValorActual(Float valorActual) { this.valorActual = valorActual; }
    public String getIpUrl() { return ipUrl; }
    public void setIpUrl(String ipUrl) { this.ipUrl = ipUrl; }
    public String getMqttTopic() { return mqttTopic; }
    public void setMqttTopic(String mqttTopic) { this.mqttTopic = mqttTopic; }
    public LocalDateTime getUltimaModificacion() { return ultimaModificacion; }
    public void setUltimaModificacion(LocalDateTime t) { this.ultimaModificacion = t; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
}
