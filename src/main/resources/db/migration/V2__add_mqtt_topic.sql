-- Migración V2: añade columna mqtt_topic si viene de la BD del TFG original
ALTER TABLE dispositivos
    ADD COLUMN mqtt_topic VARCHAR(255) DEFAULT NULL;
