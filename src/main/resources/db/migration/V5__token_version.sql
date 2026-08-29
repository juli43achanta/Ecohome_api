-- ============================================================
-- V5: Revocación de JWT — permite invalidar tokens emitidos
-- antes de un cambio de contraseña o un "cerrar sesiones activas".
-- ============================================================

ALTER TABLE usuarios
    ADD COLUMN token_version INT NOT NULL DEFAULT 0;
