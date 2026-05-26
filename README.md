# Lexicon - Sistema de Gestion de Biblioteca

## Contexto del Proyecto

La Biblioteca Central enfrenta un problema de saturacion en su sistema actual. Al ser un sistema monolitico antiguo, cada vez que el departamento de inventario carga nuevos libros, el sistema de prestamos se vuelve lento, afectando la experiencia del usuario final. Ademas, existe una falta de sincronizacion real: se prestan libros que el sistema reporta como "disponibles" pero que fisicamente ya han salido, generando reclamos y desorden administrativo.

## Propuesta (MVP)

La solucion se basa en **dos microservicios independientes** que se comunican de forma sincrona para validar las reglas de negocio en tiempo real.

---

## Arquitectura

```
+------------------+          +---------------------+
|   Libro-Service  | <------  |  Prestamo-Service   |
|   (Inventario)   |  REST    |  (Operaciones)      |
|   Puerto: 3333   |          |  Puerto: 3334       |
+------------------+          +---------------------+
       |                              |
  PostgreSQL:5433              PostgreSQL:5434
  (libro_db)                   (prestamo_db)
```

---

## Componentes

### 1. Libro-Service (Inventario) - Puerto 3333

Gestiona el catalogo de libros. Expone endpoints para busqueda filtrada mediante Query Params (autor, genero) y permite actualizar el estado de disponibilidad.

**Endpoints principales:**
- `GET /api/libros` - Listar todos los libros (con filtros: `?autor=X&genero=Y`)
- `GET /api/libros/disponibles` - Listar solo libros disponibles
- `GET /api/libros/{id}` - Obtener libro por ID
- `GET /api/libros/isbn/{isbn}` - Obtener libro por ISBN
- `POST /api/libros` - Crear nuevo libro
- `PUT /api/libros/{id}` - Actualizar libro
- `DELETE /api/libros/{id}` - Eliminar libro
- `PATCH /api/libros/{id}/disponibilidad?disponible=false` - Cambiar disponibilidad
- `GET /api/libros/{id}/disponibilidad` - Verificar disponibilidad

### 2. Prestamo-Service (Operaciones) - Puerto 3334

Orquesta el proceso de prestamo. Antes de registrar un prestamo, consulta al Libro-Service para verificar la existencia y estado del ejemplar.

**Auth endpoints:**
- `POST /api/auth/register` - Registrar usuario
- `POST /api/auth/login` - Iniciar sesion (devuelve JWT)
- `GET /api/auth/validate` - Validar token JWT

**Prestamo endpoints (requieren JWT):**
- `GET /api/prestamos` - Listar todos los prestamos
- `GET /api/prestamos/usuario` - Listar prestamos del usuario autenticado
- `GET /api/prestamos/estado?estado=ACTIVO` - Filtrar por estado
- `GET /api/prestamos/{id}` - Obtener prestamo por ID
- `POST /api/prestamos` - Registrar nuevo prestamo
- `POST /api/prestamos/{id}/devolucion` - Registrar devolucion

---

## Tecnologias

- **Java 25** con **Spring Boot 4.0.6**
- **PostgreSQL 16** (Docker)
- **Flyway** para migraciones de base de datos
- **Spring Security** + **JWT** (jjwt 0.11.5) para autenticacion
- **Lombok** para reduccion de codigo repetitivo
- **Gradle** como sistema de build
- **RestClient** para comunicacion sincrona entre servicios

---

## Inicio Rapido

### 1. Levantar las bases de datos

```bash
docker-compose up -d
```

### 2. Iniciar Libro-Service

```bash
cd libro-service
./gradlew bootRun
```

### 3. Iniciar Prestamo-Service

```bash
cd prestamo-service
./gradlew bootRun
```

### 4. Probar con los archivos `.http`

Los archivos `api.http` de cada servicio contienen todas las peticiones de ejemplo listas para ejecutar en IntelliJ IDEA o VS Code con la extension REST Client.

---

## Ejemplo de Flujo Completo

```
1. Registrar usuario:
   POST /api/auth/register  { "username": "juan", "password": "1234" }

2. Iniciar sesion:
   POST /api/auth/login     { "username": "juan", "password": "1234" }
   -> Obtiene JWT token

3. Crear libro (Libro-Service):
   POST /api/libros         { "titulo": "Don Quijote", "autor": "Cervantes", "genero": "Clasico", "isbn": "978-1-23-456789-7" }

4. Registrar prestamo (Prestamo-Service con JWT):
   POST /api/prestamos      { "libroId": "<uuid>" }
   -> Verifica disponibilidad via Libro-Service
   -> Registra prestamo
   -> Actualiza disponibilidad a false

5. Devolver libro:
   POST /api/prestamos/<id>/devolucion
   -> Actualiza estado a DEVUELTO
   -> Actualiza disponibilidad a true en Libro-Service
```

---

## Base de Datos

Cada servicio cuenta con su propia persistencia en PostgreSQL, gestionada mediante migraciones de Flyway para asegurar que el esquema sea reproducible en cualquier entorno.

### Libro-Service (libro_db)
- Tabla `libros`: id, titulo, autor, genero, isbn, disponible, created_at, updated_at
- Indices en: autor, genero, isbn, disponible

### Prestamo-Service (prestamo_db)
- Tabla `users`: id, username, password_hash, created_at, updated_at
- Tabla `prestamos`: id, libro_id, usuario_username, fecha_prestamo, fecha_devolucion, estado, created_at, updated_at
- Indices en: usuario, estado, libro_id

---

## Tests

Ambos servicios incluyen tests unitarios e integracion:

```bash
cd libro-service
./gradlew test

cd prestamo-service
./gradlew test
```
