# Arquitectura de Lexicon

## Diagrama C2 - Contenedores

Este diagrama muestra los contenedores principales del sistema y cómo se comunican.

```mermaid
flowchart TB 
  %% Actor 
  Usuario[(Usuario)]

  %% Libro-Service 
  subgraph LibroService ["Libro-Service (Puerto 3333)"] 
    direction TB 
    LibroApi["Libro API\n(Spring Boot)"] 
    LibroServiceComp["LibroService\n(Lógica de negocio)"] 
    LibroRepo["LibroRepository\n(JPA)"] 
    LibroDB[(PostgreSQL\nlibro_db)] 
    
    LibroApi --> LibroServiceComp 
    LibroServiceComp --> LibroRepo 
    LibroRepo --> LibroDB 
  end

  %% Prestamo-Service 
  subgraph PrestamoService ["Prestamo-Service (Puerto 3334)"] 
    direction TB 
    AuthApi["Auth API\n(Spring Boot)"] 
    PrestamoApi["Prestamo API\n(Spring Boot)"] 
    AuthServiceComp["AuthService\n(Registro, login, JWT)"] 
    PrestamoServiceComp["PrestamoService\n(Lógica de préstamos)"] 
    UserRepo["UserRepository\n(JPA)"] 
    PrestamoRepo["PrestamoRepository\n(JPA)"] 
    PrestamoDB[(PostgreSQL\nprestamo_db)] 
    JwtFilter["JwtAuthenticationFilter\n(Spring Security)"] 
    LibroClient["LibroServiceClientImpl\n(RestClient)"] 

    PrestamoApi --> PrestamoServiceComp 
    PrestamoApi --> JwtFilter 
    PrestamoServiceComp --> PrestamoRepo 
    PrestamoServiceComp --> LibroClient 
    AuthApi --> AuthServiceComp 
    AuthServiceComp --> UserRepo 
    PrestamoRepo --> PrestamoDB 
    UserRepo --> PrestamoDB 
  end

  %% Relaciones e interacciones del Usuario y Servicios
  Usuario -->|HTTP/JSON| LibroApi
  Usuario -->|HTTP/JSON + JWT| PrestamoApi
  Usuario -->|Registra/Inicia sesión| AuthApi 
  Usuario -->|Solicita préstamos| PrestamoApi 
  LibroClient -->|HTTP/JSON| LibroApi


## Explicación C2

- `Usuario` es el actor externo que consume los servicios via HTTP.
- `Libro-Service` es un microservicio independiente responsable del catálogo de libros.
- `Prestamo-Service` gestiona usuarios y préstamos, y se apoya en `Libro-Service` para validar disponibilidad.
- Cada servicio tiene su propia base de datos PostgreSQL.
- La comunicación entre servicios es síncrona y basada en REST.

## Contenedores principales

1. `Libro-Service`
   - `Libro API`: expone endpoints CRUD y de disponibilidad.
   - `LibroService`: lógica de consulta, creación, actualización y estado de libros.
   - `LibroRepository`: persistencia JPA.
   - `PostgreSQL libro_db`: almacena la tabla `libros`.

2. `Prestamo-Service`
   - `Auth API`: endpoints `/api/auth/register`, `/api/auth/login`, `/api/auth/validate`.
   - `Prestamo API`: endpoints `/api/prestamos`, `/api/prestamos/usuario`, `/api/prestamos/{id}`, `/api/prestamos/{id}/devolucion`.
   - `AuthService`: gestiona usuarios y tokens JWT.
   - `PrestamoService`: registra préstamos, valida disponibilidad y realiza devoluciones.
   - `LibroServiceClientImpl`: cliente REST que consulta y actualiza libros en `Libro-Service`.
   - `JwtAuthenticationFilter`: asegura que solo peticiones con JWT válido pasen a los endpoints protegidos.
   - `PostgreSQL prestamo_db`: almacena tablas `users` y `prestamos`.

## Flujo de préstamo simplificado

1. El usuario obtiene un JWT desde `Auth API`.
2. El usuario solicita un préstamo a `Prestamo API` con JWT.
3. `PrestamoService` usa `LibroServiceClientImpl` para verificar el libro en `Libro-Service`.
4. Si el libro está disponible, se guarda el préstamo en `prestamo_db` y se actualiza la disponibilidad en `libro_db`.

## Cómo usarlo

- Abre este archivo en VS Code.
- Usa el preview de Markdown o el soporte de Mermaid para visualizar el diagrama.
- Si necesitas un diagrama C2 más detallado con componentes internos adicionales, puedo agregarlo.
