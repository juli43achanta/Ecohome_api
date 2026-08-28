-- ============================================================
-- EcoHome API - Migracion V1: schema completo
-- Flyway ejecuta esto solo si la BD esta vacia.
-- Si ya tienes la BD creada con setup_completo.sql, Flyway
-- detecta baseline-on-migrate=true y no lo vuelve a aplicar.
-- ============================================================

CREATE TABLE IF NOT EXISTS usuarios (
    id       INT          AUTO_INCREMENT PRIMARY KEY,
    nombre   VARCHAR(100) NOT NULL,
    email    VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    rol      ENUM('ADMIN','USER') DEFAULT 'USER'
);

CREATE TABLE IF NOT EXISTS dispositivos (
    id                  INT          AUTO_INCREMENT PRIMARY KEY,
    nombre              VARCHAR(100) NOT NULL,
    tipo                ENUM('CALEFACCION','CAMARA','LUZ') NOT NULL,
    estado              BOOLEAN      DEFAULT FALSE,
    valor_actual        FLOAT        DEFAULT 20.0,
    ip_url              VARCHAR(255) DEFAULT NULL,
    mqtt_topic          VARCHAR(255) DEFAULT NULL,
    ultima_modificacion TIMESTAMP    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    usuario_id          INT          NOT NULL,
    CONSTRAINT fk_dispositivo_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS historial_actividad (
    id                 INT          AUTO_INCREMENT PRIMARY KEY,
    usuario_id         INT          NOT NULL,
    dispositivo_nombre VARCHAR(100) NOT NULL,
    tipo               VARCHAR(20)  NULL,
    descripcion        VARCHAR(255) NOT NULL,
    fecha_hora         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_historial_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
);
