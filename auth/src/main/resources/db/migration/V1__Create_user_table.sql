-- Creacion de la tabla de usuarios para el servicio de autenticacion --

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    email VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Índice para optimizar la autenticación y búsquedas por nombre de usuario
CREATE INDEX idx_users_username ON users(username);
-- Índice para optimizar búsquedas por email
CREATE INDEX idx_users_email ON users(email);