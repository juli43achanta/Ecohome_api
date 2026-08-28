-- V3: Los usuarios creados con el TFG original tienen password en texto plano o SHA-256.
-- Esta migración no puede hashear con BCrypt desde SQL, así que marca una contraseña
-- temporal que fuerza al usuario a cambiarla en el primer login.
--
-- IMPORTANTE: Ejecutar el script Java AdminMigration.java UNA VEZ después del primer
-- arranque de la API para hashear correctamente con BCrypt.
-- Mientras tanto, el ADMIN puede usar /api/auth/registro para recrear su cuenta.

-- Solo actúa si el password NO empieza por $2a$ (BCrypt prefix)
UPDATE usuarios
SET password = '$2a$10$PLACEHOLDER_TEMPORAL_CAMBIAR'
WHERE password NOT LIKE '$2a$%'
  AND password NOT LIKE '$2b$%';
