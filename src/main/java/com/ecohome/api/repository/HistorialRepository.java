package com.ecohome.api.repository;

import com.ecohome.api.model.HistorialActividad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistorialRepository extends JpaRepository<HistorialActividad, Integer> {
    List<HistorialActividad> findByUsuarioIdOrderByFechaHoraDesc(Integer usuarioId);

    void deleteByUsuarioId(Integer usuarioId);
}
