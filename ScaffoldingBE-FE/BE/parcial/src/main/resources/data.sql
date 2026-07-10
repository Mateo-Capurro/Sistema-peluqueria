-- ============================================================
-- SAMPLE DATA — Desarrollo / Demostración
-- ============================================================
-- ACTIVO por defecto (spring.sql.init.mode=always).
-- Usa MERGE INTO (H2 upsert con KEY por unique constraint)
-- para ser seguro ante reinicios dentro del mismo JVM.
-- ============================================================

-- USERS (passwords BCrypt: admin123 / user123 / admin123)
MERGE INTO users (username, password, name, email, telefono, dni, role, created_at, updated_at) KEY(username)
VALUES ('admin', '$2b$10$3S51YwiS.XtwR1Z2cOpoHODh44xGokP8lzAsKHkb9IboesTKFXdwy', 'Admin User', 'admin@peluqueria.com', '1100000000', '30000001', 'ADMIN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

MERGE INTO users (username, password, name, email, telefono, dni, role, created_at, updated_at) KEY(username)
VALUES ('user', '$2b$10$3S51YwiS.XtwR1Z2cOpoHOOVSgKHqE/s7wuJ3V00vR/CMR.u5ujim', 'Regular User', 'user@peluqueria.com', '1100000002', '30000002', 'CLIENTE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

MERGE INTO users (username, password, name, email, telefono, dni, role, created_at, updated_at) KEY(username)
VALUES ('jdoe', '$2b$10$3S51YwiS.XtwR1Z2cOpoHODh44xGokP8lzAsKHkb9IboesTKFXdwy', 'John Doe', 'jdoe@peluqueria.com', '1100000003', '30000003', 'CLIENTE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- PELUQUEROS (password BCrypt: admin123)
MERGE INTO users (username, password, name, email, telefono, dni, role, created_at, updated_at) KEY(username)
VALUES ('mrodriguez', '$2b$10$3S51YwiS.XtwR1Z2cOpoHODh44xGokP8lzAsKHkb9IboesTKFXdwy', 'Marta Rodriguez', 'mrodriguez@peluqueria.com', '1100000010', '30000010', 'PELUQUERO', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

MERGE INTO users (username, password, name, email, telefono, dni, role, created_at, updated_at) KEY(username)
VALUES ('lgomez', '$2b$10$3S51YwiS.XtwR1Z2cOpoHODh44xGokP8lzAsKHkb9IboesTKFXdwy', 'Lucas Gomez', 'lgomez@peluqueria.com', '1100000011', '30000011', 'PELUQUERO', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

MERGE INTO peluqueros (user_id, hora_inicio, hora_fin, activo) KEY(user_id)
VALUES ((SELECT id FROM users WHERE username = 'mrodriguez'), '09:00:00', '18:00:00', TRUE);

MERGE INTO peluqueros (user_id, hora_inicio, hora_fin, activo) KEY(user_id)
VALUES ((SELECT id FROM users WHERE username = 'lgomez'), '10:00:00', '19:00:00', TRUE);


-- TRATAMIENTOS
MERGE INTO tratamientos (nombre, duracion_minutos, precio, activo) KEY(nombre)
VALUES ('Corte pelo corto', 30, 4000.00, TRUE);

MERGE INTO tratamientos (nombre, duracion_minutos, precio, activo) KEY(nombre)
VALUES ('Corte pelo largo', 45, 5500.00, TRUE);

MERGE INTO tratamientos (nombre, duracion_minutos, precio, activo) KEY(nombre)
VALUES ('Barba', 20, 3000.00, TRUE);

MERGE INTO tratamientos (nombre, duracion_minutos, precio, activo) KEY(nombre)
VALUES ('Corte + Barba', 60, 7500.00, TRUE);


-- TURNO DEMO (fecha lejana; insert directo, no pasa por validacion de reserva)
MERGE INTO turnos (cliente_id, peluquero_id, tratamiento_id, inicio, fin, estado, created_at, updated_at)
KEY(cliente_id, peluquero_id, inicio)
VALUES (
    (SELECT id FROM users WHERE username = 'user'),
    (SELECT id FROM peluqueros WHERE user_id = (SELECT id FROM users WHERE username = 'mrodriguez')),
    (SELECT id FROM tratamientos WHERE nombre = 'Corte pelo corto'),
    '2030-07-16 10:00:00', '2030-07-16 10:30:00', 'PENDIENTE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);


