package com.ecohome.api.security;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;

class LoginRateLimiterTest {

    @Test
    void permiteHastaElMaximoDeIntentosYLuegoBloquea() {
        LoginRateLimiter limiter = new LoginRateLimiter();

        for (int i = 0; i < 5; i++) {
            assertThat(limiter.permitido("1.2.3.4")).isTrue();
        }
        assertThat(limiter.permitido("1.2.3.4")).isFalse();
    }

    @Test
    void ipsDistintasTienenContadoresIndependientes() {
        LoginRateLimiter limiter = new LoginRateLimiter();

        for (int i = 0; i < 5; i++) {
            limiter.permitido("1.1.1.1");
        }
        assertThat(limiter.permitido("1.1.1.1")).isFalse();
        assertThat(limiter.permitido("2.2.2.2")).isTrue();
    }

    @SuppressWarnings("unchecked")
    @Test
    void limpiarPurgaVentanasCaducadasParaEvitarCrecimientoIndefinido() {
        LoginRateLimiter limiter = new LoginRateLimiter();
        limiter.permitido("3.3.3.3");

        ConcurrentHashMap<String, Object> ventanas =
                (ConcurrentHashMap<String, Object>) ReflectionTestUtils.getField(limiter, "ventanas");
        assertThat(ventanas).hasSize(1);

        // Simula que la ventana de esa IP ya caducó hace tiempo
        Object ventanaVieja = crearVentana(-120_000L);
        ventanas.put("3.3.3.3", ventanaVieja);

        limiter.limpiar();

        assertThat(ventanas).isEmpty();
    }

    private Object crearVentana(long haceMs) {
        try {
            Class<?> ventanaClass = Class.forName(
                    "com.ecohome.api.security.LoginRateLimiter$Ventana");
            var constructor = ventanaClass.getDeclaredConstructor(
                    java.util.concurrent.atomic.AtomicInteger.class, long.class);
            constructor.setAccessible(true);
            return constructor.newInstance(
                    new java.util.concurrent.atomic.AtomicInteger(1),
                    System.currentTimeMillis() + haceMs);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
