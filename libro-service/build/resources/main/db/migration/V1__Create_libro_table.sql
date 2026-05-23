CREATE TABLE libros (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    titulo VARCHAR(255) NOT NULL,
    autor VARCHAR(255) NOT NULL,
    genero VARCHAR(100) NOT NULL,
    isbn VARCHAR(20) UNIQUE NOT NULL,
    disponible BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_libros_autor ON libros(autor);
CREATE INDEX idx_libros_genero ON libros(genero);
CREATE INDEX idx_libros_isbn ON libros(isbn);
CREATE INDEX idx_libros_disponible ON libros(disponible);
