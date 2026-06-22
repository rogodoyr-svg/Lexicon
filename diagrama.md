# Lexicon — Diagramas de Arquitectura Técnica

> Documentación visual técnica y profesional del ecosistema de microservicios **Lexicon** (Sistema de Gestión de Biblioteca con patrón **BFF**).
>
> Todos los diagramas utilizan **Mermaid** y se renderizan automáticamente en **GitHub**, **GitLab** y **Mermaid Live Editor** (https://mermaid.live).
>
> Metodología: **C4 Model** (Contexto → Contenedor → Componente) complementada con diagramas de secuencia y modelo de datos.

---

## Tabla de Contenidos

1. [Nivel 1 — Diagrama de Contexto del Sistema (C4)](#nivel-1--diagrama-de-contexto-del-sistema-c4)
2. [Nivel 2 — Diagrama de Contenedores (C4)](#nivel-2--diagrama-de-contenedores-c4)
3. [Nivel 3 — Diagrama de Componentes (C4)](#nivel-3--diagrama-de-componentes-c4)
4. [Topología de Red y Puertos](#topología-de-red-y-puertos)
5. [Flujo de Seguridad — JWT y BFF](#flujo-de-seguridad--jwt-y-bff)
6. [Diagrama de Secuencia — Autenticación (Registro / Login)](#diagrama-de-secuencia--autenticación-registro--login)
7. [Diagrama de Secuencia — Préstamo de Libro](#diagrama-de-secuencia--préstamo-de-libro)
8. [Diagrama de Secuencia — Devolución de Libro](#diagrama-de-secuencia--devolución-de-libro)
9. [Modelo de Datos — Database-per-Service](#modelo-de-datos--database-per-service)
10. [Matriz de Endpoints Públicos del BFF](#matriz-de-endpoints-públicos-del-bff)
11. [Stack Tecnológico](#stack-tecnológico)

---

## Nivel 1 — Diagrama de Contexto del Sistema (C4)

Vista de alto nivel: actores externos y el sistema Lexicon como una única caja negra.

```mermaid
```mermaid
C4Context
    title Lexicon — Diagrama de Contexto del Sistema (Nivel 1)

    Person(lector, "Lector / Usuario Final", "Consulta el catálogo y solicita préstamos de libros")
    Person(admin, "Personal Administrativo", "Gestiona el inventario de obras y operaciones de devolución")

    System_Boundary(sistema, "Sistema Lexicon") {
        System(lexicon, "Ecosistema Lexicon", "Plataforma distribuida de gestión de biblioteca basada en microservicios con patrón BFF")
    }

    System_Ext(docker, "Docker Engine", "Motor de contenedores que virtualiza instancias PostgreSQL aisladas")

    Rel(lector, lexicon, "Consulta catálogo, solicita préstamos", "HTTPS / JSON / JWT")
    Rel(admin, lexicon, "Registra obras, gestiona devoluciones", "HTTPS / JSON / JWT")
    Rel(lexicon, docker, "Provisiona bases de datos aisladas", "Docker API")

    UpdateRelStyle(lector, lexicon, $offsetX="-40", $offsetY="-10")
    UpdateRelStyle(admin, lexicon, $offsetX="40", $offsetY="-10")
```

---

## Nivel 2 — Diagrama de Contenedores (C4)

Desglose del ecosistema en sus contenedores desplegables independientes. Cada microservicio posee su propia base de datos (**Database-per-Service**), garantizando bajo acoplamiento y alta cohesión.

```mermaid
C4Container
    title Lexicon — Diagrama de Contenedores (Nivel 2)

    Person(lector, "Lector", "Usuario final")
    Person(admin, "Admin", "Personal administrativo")

    System_Boundary(edge, "Capa de Borde / Edge") {
        Container(bff, "Backend For Frontend (BFF)", "Java 25 · Spring Boot 4.x · Spring Security 7.x", "Puerta de enlace única. Enrutamiento centralizado, JwtAuthenticationFilter con validación SHA-256 local y propagación de peticiones a la red interna.")
    }

    System_Boundary(core, "Red Interna de Microservicios") {
        Container(auth, "auth-service", "Java 25 · Spring Boot 4.x", "Gestión de identidad: registro, login, emisión y firma criptográfica de JWT. Hashing BCrypt.")
        Container(msbook, "ms-book", "Java 25 · Spring Boot 4.x", "Catálogo descriptivo: libros, autores, géneros, ISBN. Control de existencias físicas y lógicas.")
        Container(msloan, "ms-loan", "Java 25 · Spring Boot 4.x", "Ciclo transaccional de préstamos y devoluciones. Consume ms-book vía RestClient síncrono.")
    }

    System_Boundary(data, "Capa de Persistencia (Docker)") {
        ContainerDb(authdb, "authdb", "PostgreSQL", "Puerto 5435 · Credenciales de usuario (hash BCrypt)")
        ContainerDb(librodb, "libro_db", "PostgreSQL", "Puerto 5433 · Tabla libros con índices en autor, genero, isbn, disponible")
        ContainerDb(prestamodb, "prestamo_db", "PostgreSQL", "Puerto 5434 · Tablas users y prestamos con índices de auditoría")
    }

    Rel(lector, bff, "Solicita préstamos, consulta catálogo", "HTTPS/JSON/JWT")
    Rel(admin, bff, "Registra obras, gestiona devoluciones", "HTTPS/JSON/JWT")

    Rel(bff, auth, "POST /register · POST /login", "HTTP/JSON (interno)")
    Rel(bff, msbook, "GET/POST /api/v1/libros", "HTTP/JSON (interno)")
    Rel(bff, msloan, "POST /api/v1/prestamos · devolución", "HTTP/JSON/JWT (interno)")

    Rel(msloan, msbook, "Valida reglas de negocio en tiempo real", "RestClient síncrono (HTTP/JSON)")

    Rel(auth, authdb, "Lee/escribe credenciales", "JDBC · Puerto 5435")
    Rel(msbook, librodb, "Lee/escribe catálogo", "JDBC · Puerto 5433")
    Rel(msloan, prestamodb, "Lee/escribe préstamos", "JDBC · Puerto 5434")

    UpdateRelStyle(bff, auth, $offsetX="-50", $offsetY="0")
    UpdateRelStyle(bff, msbook, $offsetX="0", $offsetY="-10")
    UpdateRelStyle(bff, msloan, $offsetX="50", $offsetY="0")
    UpdateRelStyle(msloan, msbook, $offsetX="0", $offsetY="10", $lineColor="#dc2626")
```

---

## Nivel 3 — Diagrama de Componentes (C4)

Vista interna de cada contenedor, mostrando los componentes clave (filtros, controladores, servicios, repositorios) y sus responsabilidades técnicas.

```mermaid
C4Component
    title Lexicon — Diagrama de Componentes (Nivel 3)

    Person(usuario, "Usuario / Admin", "Consumidor del BFF")

    System_Boundary(bffsys, "BFF — Puerto 8080") {
        Component(filter, "JwtAuthenticationFilter", "Spring Security 7.x · Filtro", "Pre-validación local de firma JWT mediante SHA-256 antes de propagar la petición a la red interna")
        Component(router, "Gateway Router / Dispatch", "Spring Web", "Enrutamiento centralizado hacia auth-service, ms-book o ms-loan según el path")
        Component(propagator, "Header Propagator", "Spring Security", "Propaga el JWT y cabeceras de traza hacia los microservicios downstream")
    }

    System_Boundary(authsys, "auth-service — Puerto 7778") {
        Component(authctrl, "AuthController", "Spring Web", "Expone /register y /login")
        Component(authsvc, "AuthService", "Spring Service", "Lógica de registro y autenticación")
        Component(encoder, "PasswordEncoder (BCrypt)", "Spring Security", "Hashing criptográfico de contraseñas")
        Component(jwtp, "JwtProvider", "io.jsonwebtoken", "Emisión y firma criptográfica de tokens JWT")
        Component(authrepo, "UserRepository", "Spring Data JPA", "Persistencia de credenciales")
    }

    System_Boundary(booksys, "ms-book — Puerto 3333") {
        Component(bookctrl, "LibroController", "Spring Web", "Expone /api/v1/libros y /disponibles")
        Component(booksvc, "LibroService", "Spring Service", "Reglas de inventario y existencias")
        Component(validator, "LibroRequestDto Validator", "Jakarta Validation", "Validación estricta @NotBlank del DTO inmutable")
        Component(bookrepo, "LibroRepository", "Spring Data JPA", "Acceso a libro_db con índices optimizados")
    }

    System_Boundary(loansys, "ms-loan — Puerto 3334") {
        Component(loanctrl, "PrestamoController", "Spring Web", "Expone /api/v1/prestamos y /devolucion")
        Component(loansvc, "PrestamoService", "Spring Service", "Orquestación transaccional de préstamos y devoluciones")
        Component(restclient, "RestClient", "Spring 6 RestClient", "Consumo síncrono de ms-book para validación en tiempo real")
        Component(loanrepo, "PrestamoRepository", "Spring Data JPA", "Persistencia de préstamos y auditoría de estados")
    }

    Rel(usuario, filter, "Envía request con Authorization: Bearer <JWT>", "HTTPS")
    Rel(filter, router, "Petición validada localmente (SHA-256)")
    Rel(router, propagator, "Dispatch")
    Rel(propagator, authctrl, "POST /register, /login")
    Rel(propagator, bookctrl, "/api/v1/libros")
    Rel(propagator, loanctrl, "/api/v1/prestamos")

    Rel(authctrl, authsvc, "Delega")
    Rel(authsvc, encoder, "Hash BCrypt")
    Rel(authsvc, jwtp, "Genera JWT")
    Rel(authsvc, authrepo, "Persiste usuario")

    Rel(bookctrl, booksvc, "Delega")
    Rel(bookctrl, validator, "Valida DTO")
    Rel(booksvc, bookrepo, "CRUD libros")

    Rel(loanctrl, loansvc, "Delega")
    Rel(loansvc, restclient, "Valida disponibilidad del libro", "HTTP síncrono")
    Rel(restclient, bookctrl, "GET /api/v1/libros/disponibles", "HTTP/JSON interno")
    Rel(loansvc, loanrepo, "Persiste préstamo y estado")

    UpdateRelStyle(loansvc, restclient, $lineColor="#dc2626")
    UpdateRelStyle(restclient, bookctrl, $lineColor="#dc2626", $offsetX="-30", $offsetY="10")
```

---

## Topología de Red y Puertos

Mapa de puertos expuestos por contenedor y dirección de las dependencias síncronas.

```mermaid
flowchart LR
    subgraph CLIENT["Capa Cliente"]
        UI["Aplicación Cliente<br/>(Web / Mobile)"]
    end

    subgraph EDGE["Capa de Borde · Puerto 8080"]
        BFF["🔀 BFF<br/>JwtAuthenticationFilter<br/>SHA-256 local"]
    end

    subgraph MS["Red Interna de Microservicios"]
        AUTH["🔐 auth-service<br/>Puerto 7778<br/>BCrypt + JWT"]
        BOOK["📚 ms-book<br/>Puerto 3333<br/>Inventario"]
        LOAN["🔄 ms-loan<br/>Puerto 3334<br/>Operaciones"]
    end

    subgraph DB["Capa de Persistencia · Docker"]
        AUTHDB[("authdb<br/>PG:5435")]
        BOOKDB[("libro_db<br/>PG:5433")]
        LOANDB[("prestamo_db<br/>PG:5434")]
    end

    UI -->|"HTTP / JSON / JWT<br/>:8080"| BFF

    BFF -->|":7778 /register, /login"| AUTH
    BFF -->|":3333 /api/v1/libros"| BOOK
    BFF -->|":3334 /api/v1/prestamos"| LOAN

    LOAN -.->|"RestClient síncrono<br/>valida reglas de negocio<br/>:3333"| BOOK

    AUTH --> AUTHDB
    BOOK --> BOOKDB
    LOAN --> LOANDB

    classDef edge fill:#fef3c7,stroke:#d97706,stroke-width:2px,color:#78350f
    classDef auth fill:#fee2e2,stroke:#dc2626,stroke-width:2px,color:#7f1d1d
    classDef book fill:#dcfce7,stroke:#16a34a,stroke-width:2px,color:#14532d
    classDef loan fill:#ede9fe,stroke:#7c3aed,stroke-width:2px,color:#4c1d95
    classDef db fill:#f1f5f9,stroke:#475569,stroke-width:2px,color:#0f172a
    classDef client fill:#e0e7ff,stroke:#4338ca,stroke-width:2px,color:#312e81

    class BFF edge
    class AUTH auth
    class BOOK book
    class LOAN loan
    class AUTHDB,BOOKDB,LOANDB db
    class UI client
```

---

## Flujo de Seguridad — JWT y BFF

Detalle del pipeline de seguridad implementado por el **BFF** como único punto de entrada:

```mermaid
flowchart TD
    A["Cliente envía request<br/>Authorization: Bearer JWT"] --> B{"¿Endpoint público?<br/>/register, /login"}
    B -->|Sí| Z["Propaga a auth-service<br/>sin validación JWT"]
    B -->|No| C["JwtAuthenticationFilter<br/>intercepta la petición"]

    C --> D["Validación local SHA-256<br/>de la firma del JWT"]
    D --> E{"¿Firma válida?"}
    E -->|No| F["❌ 401 Unauthorized"]
    E -->|Sí| G["Extrae claims<br/>(username, roles, exp)"]

    G --> H{"¿Token expirado?"}
    H -->|Sí| F
    H -->|No| I["Inyecta SecurityContext<br/>con principal autenticado"]

    I --> J["Gateway Router<br/>determina microservicio destino"]
    J --> K["Header Propagator<br/>reenvía JWT downstream"]
    K --> L["Microservicio interno<br/>re-valida y procesa"]

    classDef decision fill:#fef9c3,stroke:#ca8a04,stroke-width:2px
    classDef error fill:#fee2e2,stroke:#dc2626,stroke-width:2px,color:#7f1d1d
    classDef ok fill:#dcfce7,stroke:#16a34a,stroke-width:2px,color:#14532d
    classDef proc fill:#f1f5f9,stroke:#475569,stroke-width:2px

    class B,E,H decision
    class F error
    class Z,I,L ok
    class A,C,D,G,J,K proc
```

---

## Diagrama de Secuencia — Autenticación (Registro / Login)

Flujo completo del proceso de identidad, desde el registro hasta la emisión del JWT.

```mermaid
sequenceDiagram
    autonumber
    actor U as Usuario
    participant BFF as BFF :8080
    participant AUTH as auth-service :7778
    participant ENC as BCrypt Encoder
    participant JWT as JwtProvider
    participant DB as authdb :5435

    rect rgb(236, 253, 245)
    Note over U,DB: 📝 Registro de Usuario (POST /register)
    U->>BFF: POST /register {username, password}
    BFF->>AUTH: Propaga petición (endpoint público)
    AUTH->>ENC: hashPassword(plaintext)
    ENC-->>AUTH: password_hash (BCrypt)
    AUTH->>DB: INSERT user (username, password_hash)
    DB-->>AUTH: OK
    AUTH-->>BFF: 201 Created
    BFF-->>U: 201 Created
    end

    rect rgb(254, 243, 199)
    Note over U,DB: 🔐 Login (POST /login)
    U->>BFF: POST /login {username, password}
    BFF->>AUTH: Propaga petición
    AUTH->>DB: findByUsername(username)
    DB-->>AUTH: user (con password_hash)
    AUTH->>ENC: matches(plaintext, hash)
    ENC-->>AUTH: true / false
    alt Credenciales válidas
        AUTH->>JWT: generateToken(username, roles)
        JWT-->>AUTH: JWT firmado criptográficamente
        AUTH-->>BFF: AuthResponse { jwt }
        BFF-->>U: 200 OK { AuthResponse }
    else Credenciales inválidas
        AUTH-->>BFF: 401 Unauthorized
        BFF-->>U: 401 Unauthorized
    end
    end
```

---

## Diagrama de Secuencia — Préstamo de Libro

Flujo transaccional síncrono que involucra al BFF, ms-loan y la validación en tiempo real contra ms-book vía **RestClient**.

```mermaid
sequenceDiagram
    autonumber
    actor U as Lector
    participant BFF as BFF :8080
    participant LOAN as ms-loan :3334
    participant BOOK as ms-book :3333
    participant LDB as prestamo_db :5434
    participant BDB as libro_db :5433

    U->>BFF: POST /api/v1/prestamos {libroId: UUID}
    Note over BFF: JwtAuthenticationFilter<br/>valida firma SHA-256 localmente
    BFF->>LOAN: Propaga con JWT

    rect rgb(254, 226, 226)
    Note over LOAN,BOOK: 🔄 Validación síncrona en tiempo real (RestClient)
    LOAN->>BOOK: GET /api/v1/libros/disponibles
    BOOK->>BDB: SELECT * FROM libros WHERE disponible = true
    BDB-->>BOOK: ResultSet
    BOOK-->>LOAN: Lista de libros disponibles
    LOAN->>LOAN: Verifica libroId ∈ disponibles
    end

    alt Libro disponible
        LOAN->>LDB: BEGIN TX
        LOAN->>LDB: INSERT prestamo (libro_id, usuario, estado=ACTIVO)
        LOAN->>BOOK: PUT/PATCH /api/v1/libros/{id} (disponible=false)
        BOOK->>BDB: UPDATE libros SET disponible=false
        BDB-->>BOOK: OK
        BOOK-->>LOAN: 200 OK
        LOAN->>LDB: COMMIT TX
        LOAN-->>BFF: 201 Created { prestamoId }
        BFF-->>U: 201 Created
    else Libro no disponible
        LOAN-->>BFF: 409 Conflict
        BFF-->>U: 409 Conflict
    end
```

---

## Diagrama de Secuencia — Devolución de Libro

Flujo de cierre del ciclo transaccional: actualiza el estado del préstamo y restaura el inventario mediante eventos REST internos.

```mermaid
sequenceDiagram
    autonumber
    actor U as Lector / Admin
    participant BFF as BFF :8080
    participant LOAN as ms-loan :3334
    participant BOOK as ms-book :3333
    participant LDB as prestamo_db :5434
    participant BDB as libro_db :5433

    U->>BFF: POST /api/v1/prestamos/{id}/devolucion
    Note over BFF: JwtAuthenticationFilter<br/>valida JWT
    BFF->>LOAN: Propaga con JWT

    LOAN->>LDB: SELECT prestamo WHERE id = {id}
    LDB-->>LOAN: prestamo (libro_id, estado)

    alt Préstamo ACTIVO
        LOAN->>LDB: BEGIN TX
        LOAN->>LDB: UPDATE prestamo SET estado=DEVUELTO, fecha_devolucion=NOW()
        LOAN->>BOOK: PATCH /api/v1/libros/{libroId} (disponible=true)
        Note over LOAN,BOOK: Evento REST interno<br/>actualiza inventario físico
        BOOK->>BDB: UPDATE libros SET disponible=true
        BDB-->>BOOK: OK
        BOOK-->>LOAN: 200 OK
        LOAN->>LDB: COMMIT TX
        LOAN-->>BFF: 200 OK
        BFF-->>U: 200 OK
    else Préstamo no existe / ya devuelto
        LOAN-->>BFF: 404 / 409
        BFF-->>U: Error
    end
```

---

## Modelo de Datos — Database-per-Service

Cada microservicio posee su propio esquema de base de datos aislado, gestionado mediante migraciones evolutivas con **Flyway** (`db/migration/`).

```mermaid
erDiagram
    %% ===== authdb (auth-service · Puerto 5435) =====
    USERS_AUTH {
        uuid id PK
        string username UK
        string password_hash "BCrypt"
        timestamp created_at
        timestamp updated_at
    }

    %% ===== libro_db (ms-book · Puerto 5433) =====
    LIBROS {
        uuid id PK
        string titulo
        string autor "INDEX"
        string genero "INDEX"
        string isbn "INDEX"
        boolean disponible "INDEX"
        timestamp created_at
        timestamp updated_at
    }

    %% ===== prestamo_db (ms-loan · Puerto 5434) =====
    USERS_LOAN {
        uuid id PK
        string username UK "INDEX"
        string password_hash
        timestamp created_at
        timestamp updated_at
    }

    PRESTAMOS {
        uuid id PK
        uuid libro_id "INDEX (referencia lógica, no FK)"
        string usuario_username "INDEX"
        timestamp fecha_prestamo
        timestamp fecha_devolucion
        string estado "INDEX: ACTIVO | DEVUELTO"
        timestamp created_at
        timestamp updated_at
    }

    USERS_LOAN ||--o{ PRESTAMOS : "solicita"
    LIBROS ..> PRESTAMOS : "referencia lógica<br/>(no hay FK física:<br/>Database-per-Service)"
```

> **Nota de diseño:** La relación `PRESTAMOS.libro_id → LIBROS.id` es **lógica**, no física. Al adoptar el patrón *Database-per-Service*, no existen claves foráneas cross-base; la integridad referencial se garantiza en tiempo de ejecución mediante la validación síncrona de `ms-loan` contra `ms-book` vía `RestClient`.

---

## Matriz de Endpoints Públicos del BFF

Todos los endpoints externos se exponen **exclusivamente** a través del BFF (Puerto `8080`). La red interna de microservicios permanece aislada.

```mermaid
flowchart LR
    subgraph PUB["Endpoints Públicos (sin JWT)"]
        R["POST /register"]
        L["POST /login"]
    end

    subgraph SEC["Endpoints Protegidos (JWT requerido)"]
        H["GET /health"]
        GL["GET /api/v1/libros"]
        GLD["GET /api/v1/libros/disponibles"]
        PL["POST /api/v1/libros"]
        PP["POST /api/v1/prestamos"]
        PD["POST /api/v1/prestamos/{id}/devolucion"]
    end

    PUB --> BFF["BFF :8080<br/>JwtAuthenticationFilter"]
    SEC --> BFF
    BFF --> AUTH["auth :7778"]
    BFF --> BOOK["ms-book :3333"]
    BFF --> LOAN["ms-loan :3334"]

    classDef pub fill:#dcfce7,stroke:#16a34a,stroke-width:2px,color:#14532d
    classDef sec fill:#fee2e2,stroke:#dc2626,stroke-width:2px,color:#7f1d1d
    classDef bff fill:#fef3c7,stroke:#d97706,stroke-width:3px,color:#78350f
    classDef ms fill:#f1f5f9,stroke:#475569,stroke-width:2px

    class R,L pub
    class H,GL,GLD,PL,PP,PD sec
    class BFF bff
    class AUTH,BOOK,LOAN ms
```

### Detalle de Endpoints

| Método | Endpoint | Descripción | Autenticación | Microservicio Backend |
|:---:|---|---|:---:|---|
| `POST` | `/register` | Registro inicial de usuarios | ❌ Público | `auth-service` |
| `POST` | `/login` | Inicia sesión, retorna `AuthResponse` con JWT | ❌ Público | `auth-service` |
| `GET` | `/health` | Estado e infraestructura del BFF | ✅ JWT | BFF (local) |
| `GET` | `/api/v1/libros` | Lista catálogo total (`?autor=X&genero=Y`) | ❌ Público | `ms-book` |
| `GET` | `/api/v1/libros/disponibles` | Lista obras con existencias lógicas | ❌ Público | `ms-book` |
| `POST` | `/api/v1/libros` | Registra nueva obra (`LibroRequestDto`) | ❌ Público | `ms-book` |
| `POST` | `/api/v1/prestamos` | Solicita préstamo (`{"libroId": "<UUID>"}`) | ✅ JWT | `ms-loan` |
| `POST` | `/api/v1/prestamos/{id}/devolucion` | Registra retorno físico | ✅ JWT | `ms-loan` |

---

## Stack Tecnológico

```mermaid
mindmap
  root((Lexicon Stack))
    Backend
      Java 25
      Spring Boot 4.x
      Spring Security 7.x
      Spring Web
      Spring Data JPA
      Spring 6 RestClient
      Jakarta Validation @NotBlank
    Seguridad
      JWT io.jsonwebtoken
      BCrypt PasswordEncoder
      JwtAuthenticationFilter
      Validación SHA-256 local
    Persistencia
      PostgreSQL
      Docker aislado
      Flyway db/migration
      Database-per-Service
    Construcción
      Gradle
      Docker Compose
    Testing
      Tests unitarios
      Tests de integración
```

---

## Resumen de Decisiones Arquitectónicas

| Decisión | Justificación Técnica |
|---|---|
| **Patrón BFF** | Punto único de entrada que aísla la red interna, centraliza seguridad (JwtAuthenticationFilter) y desacopla el frontend de la topología de microservicios. |
| **Database-per-Service** | Bajo acoplamiento y alta cohesión: cada microservicio posee su propio PostgreSQL, evitando bloqueos cruzados y permitiendo escalado independiente. |
| **Validación SHA-256 local en BFF** | Pre-validación criptográfica de la firma JWT antes de propagar la petición, reduciendo carga en los microservicios backend. |
| **BCrypt en auth-service** | Hashing de contraseñas resistente a ataques de fuerza bruta mediante cost factor configurable. |
| **RestClient síncrono (ms-loan → ms-book)** | Garantiza consistencia en tiempo real al validar disponibilidad de libros antes de consolidar un préstamo, evitando préstamos lógicos de obras no disponibles físicamente. |
| **Flyway** | Versionado evolutivo y reproducible de esquemas en cualquier entorno. |
| **Aislamiento por puertos** | Cada servicio (BFF 8080, auth 7778, ms-book 3333, ms-loan 3334, PG 5433-5435) opera de forma independiente para despliegue y escalado granular. |

---

