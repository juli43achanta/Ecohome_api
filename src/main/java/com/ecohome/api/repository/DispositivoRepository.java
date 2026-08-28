package com.ecohome.api.repository;

import com.ecohome.api.model.Dispositivo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DispositivoRepository extends JpaRepository<Dispositivo, Integer> {
    List<Dispositivo> findByUsuarioId(Integer usuarioId);
    List<Dispositivo> findByUsuarioIdAndTipo(Integer usuarioId, Dispositivo.Tipo tipo);
    Optional<Dispositivo> findByMqttTopic(String mqttTopic);
}
