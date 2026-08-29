package com.ecohome.api.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);
    private static final String DEV_SECRET = "EcoHomeDevSecretKey2026ParaDesarrolloLocalUnicamente!!";

    @Value("${ecohome.jwt.secret}")
    private String secret;

    @Value("${ecohome.jwt.expiration-ms}")
    private long expirationMs;

    @Value("${spring.profiles.active:dev}")
    private String perfilActivo;

    @PostConstruct
    public void validarSecreto() {
        if (secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException(
                "JWT_SECRET debe tener al menos 32 caracteres para HS256. " +
                "Configura la variable de entorno JWT_SECRET.");
        }
        boolean esProduccion = perfilActivo.toLowerCase().contains("prod");
        if (DEV_SECRET.equals(secret)) {
            if (esProduccion) {
                throw new IllegalStateException(
                    "JWT_SECRET usa el valor de DESARROLLO con spring.profiles.active=" + perfilActivo + ". " +
                    "Configura la variable de entorno JWT_SECRET con un secreto real antes de desplegar.");
            }
            log.warn("⚠️  JWT_SECRET usa el valor de DESARROLLO. " +
                     "Configura la variable de entorno JWT_SECRET antes de desplegar en producción.");
        }
    }

    private SecretKey key() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generarToken(UserDetails userDetails) {
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("roles", userDetails.getAuthorities().toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key())
                .compact();
    }

    public String extraerEmail(String token) {
        return parsear(token).getPayload().getSubject();
    }

    public boolean esValido(String token, UserDetails userDetails) {
        try {
            String email = extraerEmail(token);
            return email.equals(userDetails.getUsername()) && !estaExpirado(token);
        } catch (JwtException e) {
            return false;
        }
    }

    private boolean estaExpirado(String token) {
        return parsear(token).getPayload().getExpiration().before(new Date());
    }

    private Jws<Claims> parsear(String token) {
        return Jwts.parser().verifyWith(key()).build().parseSignedClaims(token);
    }
}
