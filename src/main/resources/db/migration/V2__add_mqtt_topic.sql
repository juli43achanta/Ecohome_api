-- Migración V2: añade columna mqtt_topic solo si no existe (idempotente)
SET @exists = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME   = 'dispositivos'
      AND COLUMN_NAME  = 'mqtt_topic'
);

SET @sql = IF(@exists = 0,
    'ALTER TABLE dispositivos ADD COLUMN mqtt_topic VARCHAR(255) DEFAULT NULL',
    'SELECT 1'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
