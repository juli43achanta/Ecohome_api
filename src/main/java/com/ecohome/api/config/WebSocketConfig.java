package com.ecohome.api.config;

import com.ecohome.api.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.socket.config.annotation.*;

import java.util.List;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private static final Logger log = LoggerFactory.getLogger(WebSocketConfig.class);

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Value("${ecohome.cors.allowed-origins}")
    private String allowedOrigins;

    public WebSocketConfig(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        List<String> origins = List.of(allowedOrigins.split(","));
        registry.addEndpoint("/ws")
                .setAllowedOrigins(origins.toArray(new String[0]))
                .withSockJS();
    }

    /**
     * Valida el JWT en el frame STOMP CONNECT antes de aceptar la conexión.
     * El cliente debe enviar el token en el header STOMP "Authorization: Bearer <token>".
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor =
                        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                if (accessor == null || !StompCommand.CONNECT.equals(accessor.getCommand())) {
                    return message;
                }

                String authHeader = accessor.getFirstNativeHeader("Authorization");
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    log.warn("WebSocket CONNECT rechazado: falta Authorization header");
                    throw new org.springframework.messaging.MessagingException(
                            "Token JWT requerido para conectar al WebSocket");
                }

                String token = authHeader.substring(7);
                try {
                    String email = jwtUtil.extraerEmail(token);
                    UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                    if (!jwtUtil.esValido(token, userDetails)) {
                        throw new org.springframework.messaging.MessagingException(
                                "Token JWT inválido o expirado");
                    }
                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    accessor.setUser(auth);
                    log.debug("WebSocket autenticado para {}", email);
                } catch (org.springframework.messaging.MessagingException e) {
                    throw e;
                } catch (Exception e) {
                    log.warn("WebSocket CONNECT rechazado: {}", e.getMessage());
                    throw new org.springframework.messaging.MessagingException(
                            "Token JWT inválido: " + e.getMessage());
                }

                return message;
            }
        });
    }
}
