package com.ecohome.api.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SecretsValidator {

    private static final String DEV_DB_PASSWORD = "EcoHome2026!";

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @Value("${spring.profiles.active:dev}")
    private String perfilActivo;

    @PostConstruct
    public void validar() {
        boolean esProduccion = perfilActivo.toLowerCase().contains("prod");
        if (esProduccion && DEV_DB_PASSWORD.equals(dbPassword)) {
            throw new IllegalStateException(
                "DB_PASSWORD usa el valor de DESARROLLO con spring.profiles.active=" + perfilActivo + ". " +
                "Configura la variable de entorno DB_PASSWORD con una contraseña real antes de desplegar.");
        }
    }
}
