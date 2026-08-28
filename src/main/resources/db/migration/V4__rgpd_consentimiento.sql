-- ============================================================
-- V4: Cumplimiento RGPD Art. 7 — consentimiento explícito
-- Añade campos de consentimiento y fecha de registro a usuarios.
-- ============================================================

ALTER TABLE usuarios
    ADD COLUMN consentimiento_gdpr TINYINT(1) NOT NULL DEFAULT 0,
    ADD COLUMN fecha_registro      DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- Marcar usuarios existentes como con consentimiento previo dado
-- (datos anteriores a la implementación RGPD — ajustar política si hay usuarios reales)
UPDATE usuarios SET consentimiento_gdpr = 1 WHERE consentimiento_gdpr = 0;
