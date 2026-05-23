CREATE TABLE prestamos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    libro_id UUID NOT NULL,
    usuario_username VARCHAR(255) NOT NULL,
    fecha_prestamo TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_devolucion TIMESTAMP,
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_prestamos_libro_id ON prestamos(libro_id);
CREATE INDEX idx_prestamos_usuario ON prestamos(usuario_username);
CREATE INDEX idx_prestamos_estado ON prestamos(estado);
