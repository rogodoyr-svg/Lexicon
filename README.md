# Lexicon - Sistema de Gestión de Biblioteca (Arquitectura Distribuidora con BFF)

## Contexto del Proyecto
La Biblioteca Central enfrenta un problema histórico de saturación y degradación de rendimiento en su infraestructura tecnológica. Al operar bajo un sistema monolítico antiguo, las cargas masivas o actualizaciones en el inventario por parte del personal administrativo ralentizaban críticamente el módulo de préstamos, afectando la experiencia de cara al usuario final. Adicionalmente, la falta de sincronización en tiempo real permitía el préstamo lógico de libros que físicamente ya no se encontraban disponibles, generando desorden administrativo y reclamos continuos.

## Solución Propuesta (Nueva Arquitectura)
Para mitigar estas deficiencias, el sistema se ha rediseñado bajo una arquitectura de microservicios acoplada a un patrón **BFF (Backend For Frontend)** que centraliza la entrada de peticiones, aislando por completo la red interna de microservicios y protegiendo el ecosistema mediante tokens criptográficos **JWT**.

---

## Arquitectura del Sistema

El ecosistema de software está compuesto por 4 componentes independientes, implementando el patrón *Database-per-Service* para garantizar bajo acoplamiento y alta cohesión:


               +----------------------------------+
               |       📱 Aplicación Cliente      |
               +----------------------------------+
                                |
                                | HTTP / JSON / JWT
                                v
               +----------------------------------+
               |   🔀 Backend For Frontend (BFF)   |
               |           Puerto: 8080           |
               +----------------------------------+
                     /          |          \
                    /           |           \
                   v            v            v
           +--------------+ +--------------+ +--------------+
           | auth-service | |   ms-book    | |   ms-loan    |
           | (Seguridad)  | | (Inventario) | | (Operaciones)|
           | Puerto: 7778 | | Puerto: 3333 | | Puerto: 3334 |
           +--------------+ +--------------+ +--------------+
                  |                |                |
             PostgreSQL       PostgreSQL       PostgreSQL
            Puerto: 5435     Puerto: 5433     Puerto: 5434
              (authdb)        (libro_db)     (prestamo_db)

### Componentes del Ecosistema ##

1. **BFF (Backend For Frontend) - Puerto 8080:** Punto único de entrada para las aplicaciones cliente. Centraliza el enrutamiento y aplica un `JwtAuthenticationFilter` para pre-validar la firma de las credenciales de seguridad de manera local utilizando un algoritmo hash `SHA-256` antes de propagar las peticiones a la red interna.

2. **Auth-Service (auth) - Puerto 7778:** Microservicio especializado encargado del registro, login de usuarios y la emisión/firma de tokens criptográficos JWT con hashing de contraseñas mediante `BCrypt`, operando de manera aislada al negocio.

3. **ms-book (Inventario) - Puerto 3333:** Gestiona de forma autónoma el catálogo descriptivo de libros, autores, géneros e ISBN, controlando las existencias físicas y lógicas mediante su propia persistencia.

4. **ms-loan (Operaciones) - Puerto 3334:** Orquesta el ciclo de vida transaccional de los préstamos y devoluciones. Consume síncronamente mediante `RestClient` los endpoints de `ms-book` para validar reglas de negocio en tiempo real antes de consolidar una operación.

---

## Tecnologías Utilizadas ##
- **Java 25** con **Spring Boot 4.x** (Spring Security 7.x)
- **PostgreSQL** virtualizado independientemente mediante Docker (Módulo auth en puerto `5435`)
- **Flyway** para el control de versiones evolutivo de esquemas de bases de datos
- **JWT (JSON Web Tokens - io.jsonwebtoken)** para el esquema global de seguridad distribuida
- **Jakarta Validation Constraints** para la validación estricta de DTOs (`@NotBlank`)
- **Gradle** como sistema avanzado de automatización y construcción

---

## Estructura de Endpoints Públicos (Expuestos por el BFF) ##

Todas las interacciones desde el cliente externo se realizan exclusivamente a través de la puerta de enlace del BFF (Puerto `8080`):

### Endpoints de Autenticación ##
- `POST http://localhost:8080/register` -> Registro inicial de usuarios.
- `POST http://localhost:8080/login` -> Inicia sesión (Retorna una estructura `AuthResponse` con el JWT válido).
- `GET http://localhost:8080/health` -> Validación de estado e infraestructura del BFF (Requiere pasar el JWT en el encabezado `Authorization: Bearer <token>`).

### Endpoints de Catálogo (ms-book) ##
- `GET http://localhost:8080/api/v1/libros` -> Lista el catálogo total (Filtros opcionales: `?autor=X&genero=Y`).
- `GET http://localhost:8080/api/v1/libros/disponibles` -> Lista exclusivamente obras con existencias lógicas.
- `POST http://localhost:8080/api/v1/libros` -> Registra una nueva obra. Requiere validación de estructura inmutable `LibroRequestDto`.

### Endpoints de Operaciones (ms-loan - Requieren JWT en el Header) ##
- `POST http://localhost:8080/api/v1/prestamos` -> Solicita un nuevo préstamo (Cuerpo JSON: `{"libroId": "<UUID>"}`).
- `POST http://localhost:8080/api/v1/prestamos/{id}/devolucion` -> Registra el retorno físico, actualizando el inventario mediante eventos REST internos.

---

## Diseño y Estrategia de Persistencia ##
Cada base de datos corre de manera aislada en su respectivo contenedor PostgreSQL, administrada secuencialmente por Flyway (`db/migration/`):

- **`authdb` (Puerto 5435):** Almacena credenciales de usuario con contraseñas Hasheadas criptográficamente a nivel de esquema de seguridad.
- **`libro_db` (Puerto 5433):** Tabla `libros` optimizada con índices de búsqueda rápida en columnas de alta demanda (`autor`, `genero`, `isbn`, `disponible`).
- **`prestamo_db` (Puerto 5434):** Administra el histórico relacional mediante scripts de migración ordenados e índices de auditoría en el estado de las transacciones.

---

## Guía de Inicio Rápido ##

### 1. Inicialización de la Persistencia (Docker) ##
Levanta todos los motores PostgreSQL aislados definidos en la raíz del proyecto:
```bash
docker-compose up -d

2. Despliegue Secuencial de Servicios
Ejecuta los siguientes comandos en terminales independientes de VS Code para inicializar el ecosistema backend:

# Iniciar Componente de Autenticación (Puerto 7778)
cd auth && ./gradlew bootRun

# Iniciar Microservicio de Inventario (Puerto 3333)
cd ../ms-book && ./gradlew bootRun

# Iniciar Microservicio de Operaciones (Puerto 3334)
cd ../ms-loan && ./gradlew bootRun

# Iniciar Puerta de Enlace BFF (Puerto 8080)
cd ../bff && ./gradlew bootRun

---

Base de Datos
Cada servicio cuenta con su propia persistencia en PostgreSQL, gestionada mediante migraciones de Flyway para asegurar que el esquema sea reproducible en cualquier entorno.

ms-book (libro_db)
- Tabla libros: id, titulo, autor, genero, isbn, disponible, created_at, updated_at
- Índices en: autor, genero, isbn, disponible

ms-loan (prestamo_db)
Tabla users: id, username, password_hash, created_at, updated_at

- Tabla prestamos: id, libro_id, usuario_username, fecha_prestamo, fecha_devolucion, estado, created_at, updated_at
- Índices en: usuario, estado, libro_id

Tests
Ambos servicios incluyen tests unitarios e integración:

- cd ms-book ./gradlew test
- cd ../ms-loan ./gradlew test
