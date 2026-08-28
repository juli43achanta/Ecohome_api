# EcoHome API — Guía de arranque

## Requisitos
- JDK 17+
- Maven 3.9+
- MySQL 8 corriendo con `ecohome_db` (setup_completo.sql del proyecto JavaFX)
- Mosquitto MQTT broker (opcional para IoT real)

## 1. Instalar Mosquitto (MQTT broker)

```bash
# Windows: descargar de https://mosquitto.org/download/
# O con Chocolatey:
choco install mosquitto

# Iniciar el servicio
net start mosquitto
```

## 2. Compilar y arrancar la API

```bash
cd DAM-2026-ECOHOME_API
mvn spring-boot:run
```

La API arranca en http://localhost:8080

## 3. Endpoints disponibles

| Método | URL | Descripción | Auth |
|--------|-----|-------------|------|
| POST | /api/auth/login | Login → devuelve JWT | No |
| POST | /api/auth/registro | Registro nuevo usuario | No |
| GET | /api/dispositivos/usuario/{id} | Listar dispositivos | JWT |
| POST | /api/dispositivos/usuario/{id} | Crear dispositivo | JWT |
| PUT | /api/dispositivos/{id} | Modificar dispositivo | JWT |
| DELETE | /api/dispositivos/{id} | Eliminar dispositivo | JWT |
| POST | /api/dispositivos/{id}/toggle | ON/OFF + MQTT | JWT |
| POST | /api/dispositivos/{id}/valor?valor=22 | Setpoint | JWT |
| GET | /api/historial/usuario/{id} | Historial | JWT |
| GET | /api/usuarios | Listar usuarios (ADMIN) | JWT |

## 4. Ejemplo de uso con curl

```bash
# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"julian@ecohome.com","password":"1234"}'

# Usar el token devuelto
TOKEN="eyJ..."

# Listar dispositivos del usuario 1
curl http://localhost:8080/api/dispositivos/usuario/1 \
  -H "Authorization: Bearer $TOKEN"

# Encender/apagar dispositivo 2
curl -X POST http://localhost:8080/api/dispositivos/2/toggle \
  -H "Authorization: Bearer $TOKEN"
```

## 5. WebSocket (actualizaciones en tiempo real)

Los clientes se conectan a `ws://localhost:8080/ws` usando STOMP.
Se suscriben a `/topic/dispositivos/{id}` para recibir el estado actualizado
cada vez que el dispositivo cambia (por API o por MQTT desde hardware real).

## 6. Integración IoT — Dispositivo Tasmota

1. Flashear el dispositivo con Tasmota
2. En la consola Tasmota: `MqttHost 192.168.1.X` (IP del PC con Mosquitto)
3. En la BD: `UPDATE dispositivos SET mqtt_topic='stat/NOMBRE/POWER' WHERE id=X`
4. El dispositivo publicará su estado → la API lo recibe → actualiza BD + notifica clientes

## Siguientes pasos
- [ ] Refactor JavaFX: sustituir DAOs por llamadas HTTP
- [ ] App Android: Retrofit + STOMP WebSocket
- [ ] Deploy en Railway/Render (cloud gratuito)
- [ ] Mosquitto en HiveMQ Cloud (MQTT cloud gratuito)
