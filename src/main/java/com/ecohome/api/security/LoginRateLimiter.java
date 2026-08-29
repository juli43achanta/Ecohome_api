package com.ecohome.api.security;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class LoginRateLimiter {

    private static final int MAX_INTENTOS = 5;
    private static final long VENTANA_MS = 60_000;

    private record Ventana(AtomicInteger contador, long inicio) {}

    private final ConcurrentHashMap<String, Ventana> ventanas = new ConcurrentHashMap<>();

    public boolean permitido(String ip) {
        long ahora = System.currentTimeMillis();
        Ventana ventana = ventanas.compute(ip, (k, existente) -> {
            if (existente == null || ahora - existente.inicio() > VENTANA_MS) {
                return new Ventana(new AtomicInteger(0), ahora);
            }
            return existente;
        });
        return ventana.contador().incrementAndGet() <= MAX_INTENTOS;
    }

    // Purga IPs inactivas para evitar crecimiento no acotado del mapa
    @Scheduled(fixedRate = 300_000)
    public void limpiar() {
        long ahora = System.currentTimeMillis();
        ventanas.entrySet().removeIf(e -> ahora - e.getValue().inicio() > VENTANA_MS);
    }
}
