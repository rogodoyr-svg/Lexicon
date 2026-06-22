# Lexicon - Sistema de Gestion de Biblioteca

## Contexto del Proyecto

La Biblioteca Central enfrenta un problema de saturacion en su sistema actual. Al ser un sistema monolitico antiguo, cada vez que el departamento de inventario carga nuevos libros, el sistema de prestamos se vuelve lento, afectando la experiencia del usuario final. Ademas, existe una falta de sincronizacion real: se prestan libros que el sistema reporta como "disponibles" pero que fisicamente ya han salido, generando reclamos y desorden administrativo.

## Propuesta de solucion (MVP)

Para mitigar estas deficiencias, se ha diseñado una arquitectura distribuida y desacoplada basada en microservicios especializados para el negocio. La solución aísla las cargas transaccionales, se protege mediante un Servicio de Autenticación dedicado (auth) y expone sus recursos a las aplicaciones clientes de forma segura y unificada a través de un componente BFF (Backend For Frontend). Esto elimina la contención de recursos en las bases de datos y encapsula la complejidad interna del ecosistema distribuido.  


---

## Arquitectura

El sistema está compuesto por dos microservicios independientes, cada uno con su propio `docker-compose.yml` y su propia base de datos PostgreSQL.

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

- `libro-service/docker-compose.yml` define `postgresLibro` en el puerto local `5433`.
- `prestamo-service/docker-compose.yml` define `postgresPrestamo` en el puerto local `5434`.
- `Prestamo-Service` consume `Libro-Service` por REST para validar disponibilidad y actualizar estado.
- Cada servicio es autónomo en su lógica y persistencia, lo que facilita el mantenimiento y el despliegue independiente.

---

## Componentes

### 1. Libro-Service (Inventario) - Puerto 3333

Gestiona el catálogo de libros y su disponibilidad. Usa `libro_db` en PostgreSQL para almacenar la información de los libros.

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

Orquesta el flujo de préstamos y mantiene usuarios autenticados con JWT. Consulta `Libro-Service` antes de registrar un préstamo.

**Auth endpoints:**
- `POST /api/auth/register` - Registrar usuario
- `POST /api/auth/login` - Iniciar sesión (devuelve JWT)
- `GET /api/auth/validate` - Validar token JWT

**Prestamo endpoints (requieren JWT):**
- `GET /api/prestamos` - Listar todos los préstamos
- `GET /api/prestamos/usuario` - Listar préstamos del usuario autenticado
- `GET /api/prestamos/estado?estado=ACTIVO` - Filtrar por estado
- `GET /api/prestamos/{id}` - Obtener préstamo por ID
- `POST /api/prestamos` - Registrar nuevo préstamo
- `POST /api/prestamos/{id}/devolucion` - Registrar devolución

**ms-books endpoints:**
- `GET /api/v1/libros` - Listar catalogo completo
- `GET /api/v1/libros/disponibles` - Retorna exclusivamente los ejemplares que cuentan con existencias logicas en el inventario
- `GET /api/v1/libros/{id}/disponiblidad` - Endpoint interno y externo utilizado para verificar si un ID especifico está apto para préstamo.
- `POST /api/v1/libros` - Crear un nuevo libro

**ms-loan endpoints:**
- `POST /api/v1/prestamos` - Registrar un prestamo tras validar con el modulo de libros
- `POST /api/v1/prestamos/{id}/devolucion` - Cierre de transaccion y liberacion del libro.

  
---

## Tecnologias

- **Java 25** con **Spring Boot 4.0.6**
- **PostgreSQL 16** (Docker)
- **Flyway** para migraciones de base de datos
- **Spring Security** + **JWT** (jjwt 0.11.5) para autenticacion
- **Lombok** para reduccion de codigo repetitivo
- **Gradle** como sistema de build
- **RestClient** para comunicacion sincrona entre servicios
- **BFF / API Gateway** Spring Cloud Gateway para el enrutamiento reactivo y eficiente de las peticiones

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
