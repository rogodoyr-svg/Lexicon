------------------------------------Sistema de gestión de biblioteca "Lexicón"----------------------------

1. Contexto del caso:

La Biblioteca Central enfrenta un problema de saturación en su sistema actual. Al ser un sistema monolítico antiguo, cada vez que el departamento de inventario carga nuevos libros, el sistema de préstamos se vuelve lento, afectando la experiencia del usuario final. Además, existe una falta de sincronización real: se prestan libros que el sistema reporta como "disponibles" pero que físicamente ya han salido, generando reclamos y desorden administrativo.

--------------------------------------------------------------------------------------------------

2. Introducción:

El proyecto Lexicon nace como una propuesta de transformación digital para desacoplar las operaciones de la biblioteca. El objetivo es separar la gestión de activos (Libros) de la gestión transaccional (Préstamos).

Al implementar microservicios, garantizamos que el catálogo pueda ser consultado masivamente sin interrumpir el proceso crítico de registro de préstamos, asegurando la alta disponibilidad y la integridad de los datos.

-------------------------------------------------------------------------------------------------------

3. Propuesta (MVP)

La solución para este caso se basa en dos microservicios independientes que se comunican de forma síncrona para así validar las reglas de negocios en tiempo real.

Componentes de la solución:

* Libro-Service (Inventario): Gestiona el catálogo de libros. Expone endpoints para búsqueda filtrada mediante Query Params (autor, género) y permite actualizar el estado de disponibilidad.

* Préstamo-Service (Operaciones): Orquesta el proceso de préstamo. Antes de registrar un préstamo, consulta al Libro-Service para verificar la existencia y estado del ejemplar.

* Base de Datos Relacional: Cada servicio cuenta con su propia persistencia en PostgreSQL, gestionada mediante migraciones de Flyway para asegurar que el esquema sea reproducible en cualquier entorno.

------------------------------------------------------------------------------------------------------------
4. Requisitos no funcionales y funcionales

●	Requisitos Funcionales

RF1: Gestión de Inventario: El sistema debe permitir el registro y almacenamiento de libros con atributos de título, autor y estado de disponibilidad en una base de datos PostgreSQL.

RF2: Búsqueda Filtrada: El sistema debe permitir la consulta de libros utilizando filtros específicos (autor, género) enviados a través de Query Params.

RF3: Validación de Préstamos: El Préstamo-Service debe consultar al Libro-Service para verificar si un libro existe y si su estado es "disponible" antes de procesar una transacción.
RF4: Actualización de Disponibilidad: Una vez registrado un préstamo, el sistema debe actualizar automáticamente el estado del libro en el microservicio de inventario para evitar duplicidad de préstamos.

RF5: Control de Usuarios: El sistema debe gestionar el registro de qué usuario tiene asignado cada libro.


●	Requisitos no funcionales:

RNF1: Arquitectura de Microservicios: El sistema debe estar desacoplado en dos servicios independientes (Libros y Préstamos) para asegurar la alta disponibilidad y escalabilidad.
RNF2: Tecnologías Base: El desarrollo debe realizarse utilizando Java 25 y Spring Boot 4.
RNF3: Persistencia y Evolución de Datos: Se debe utilizar PostgreSQL como base de datos relacional y Flyway para el control de versiones y migraciones del esquema.

RNF4: Comunicación Inter-service: La comunicación entre los microservicios debe ser síncrona, utilizando RestTemplate o WebClient.

RNF5: Estándares de Código:
●	Uso de Lombok para reducir código repetitivo (getters/setters) y Records para la inmutabilidad de los DTOs.

●	Implementación de servicios basada en interfaces (IMPL) para cumplir con el principio de inversión de dependencias.

RNF6: Respuestas HTTP Estándar: El sistema debe utilizar ResponseEntity para estandarizar las respuestas y el manejo de errores hacia el cliente.
RNF7: Gestión de Versiones: El código fuente y la documentación deben estar alojados en un repositorio de GitHub con un archivo README detallado.

------------------------------------------------------------------------------------------------------------------------------------

5. Requerimientos técnicos

Para cumplir con los estándares de calidad, se han utilizado las siguientes herramientas:
●	Spring Data jpa: Para el acceso de datos.
●	Lombok y récords: Para reducir el código repetitivo y asegurar la inmutabilidad de los Dtos
●	 ResponseEntity: Para estandarizar las respuestas HTTP y el manejo de errores.
●	Interfaces "IMPL": Aplicando el principio de inversión de dependencias para asi poder facilitar las pruebas.

Backlog de historias de usuarios (MVP)

HU-01: Registro de Inventario (Libro-Service) 

●	Historia: Como administrador de la biblioteca, quisiera registrar nuevos libros en el sistema, para mantener el catálogo actualizado.

●	Criterios de aceptación:
-	Se debe utilizar una entidad “Libro” con soporte de lombok para los campos: título, autor y disponibilidad.
-	La persistencia debe realizarse en PostgreSQL mediante el uso de interfaces de repositorio.
-	La respuesta debe ser un ResponseEntity con el código HTTP 201

HU-02: Búsqueda Filtrada de Libros (Libro-Service)

●	Historia: Como usuario de la biblioteca, quiero buscar libros por autor o género utilizando filtros, para encontrar rápidamente el material que necesito.
●	Criterios de Aceptación:
○	El controlador debe recibir los criterios a través de Query Params.
○	El servicio debe devolver una lista de LibroDTO utilizando el formato récord de Java.
○	Si no hay resultados, debe retornar un código 200 con una lista vacía.
HU-03: Validación de Disponibilidad (Comunicación Inter-service)

●	Historia: Como sistema de préstamos, quiero consultar el estado de un libro en el servicio de inventario antes de procesar una solicitud, para evitar prestar libros que ya no están disponibles.
●	Criterios de Aceptación:
○	El Préstamo-Service debe comunicarse de forma síncrona con el Libro-Service usando RestTemplate o WebClient.
○	Se debe validar la existencia y el campo disponible del libro en tiempo real.
○	En caso de que el libro no exista, se debe retornar un error 404 manejado a través de ResponseEntity.
