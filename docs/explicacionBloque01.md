## Bloque 1 · Persistencia y base de seguridad (`feature/persistence`)

**Estado:** completado · **Rama:** `feature/persistence_Argalle` → `develop`

### Qué se implementó

| Componente | Paquete | Descripción |
|---|---|---|
| `Rol`, `EstadoSolicitud` | `entity` | Enums guardados como texto (`EnumType.STRING`) |
| `Usuario` | `entity` | Cuenta con `correo` único, `password_hash`, `rol` y campos opcionales de trabajador (`oficio_principal`, `zona`) |
| `Solicitud` | `entity` | Relaciona un contratante y un trabajador con un estado y una fecha de creación |
| `UsuarioRepository` | `repository` | Búsqueda por correo y verificación de existencia |
| `SolicitudRepository` | `repository` | Consultas **filtradas por participante** (contratante, trabajador o ambos) |
| `ApiError` | `dto` | Formato único de error (`timestamp`, `status`, `error`, `mensaje`, `ruta`) |
| `SecurityConfig` | `config` | `@EnableMethodSecurity`, `PasswordEncoder` (BCrypt), sesiones stateless, CSRF desactivado y reglas de acceso |
| `application.properties` | `resources` | Conexión a PostgreSQL y propiedades del JWT, leídas desde variables de entorno |
| `application-test.properties` | `test/resources` | H2 en memoria y clave del JWT solo para pruebas |

### Decisiones de diseño

- **Las consultas incluyen al participante.** Métodos como `findByIdAndContratanteId` y `findByIdAndParticipante` hacen que una solicitud ajena y una inexistente produzcan el mismo resultado vacío. Más adelante eso se traduce en un `404` y no revela que el recurso existe (RS-07).
- **Sin secretos en el repositorio.** `DB_PASSWORD` y `JWT_SECRET` no tienen valor por defecto: se leen del entorno. El archivo `.env.example` solo lista los nombres, y `.env` está en el `.gitignore` (RS-10).
- **Perfil `test` separado.** Las pruebas usan H2 en memoria con `@ActiveProfiles("test")`, así que no necesitan PostgreSQL ni las variables de entorno.
- **Regla de acceso por defecto cerrada.** Solo `/api/v1/auth/**` es público, `/api/v1/admin/**` exige `ADMIN` y todo lo demás requiere autenticación. No se usa `anyRequest().permitAll()`.
- **`toString` seguro.** En `Usuario` no se imprimen `passwordHash`, `correo` ni `telefono`, para que no terminen en los logs.

### Cómo se verificó

1. `mvn clean compile` termina en `BUILD SUCCESS`.
2. La aplicación arranca contra PostgreSQL con las variables `DB_PASSWORD` y `JWT_SECRET` definidas (`Started OficioyaApplication`).
3. Hibernate crea las tablas `usuarios` y `solicitudes` en la base `oficioya_security`.
4. `mvn clean verify` pasa usando H2 con el perfil `test`.

### Problemas encontrados y cómo se resolvieron

| Problema | Causa | Solución |
|---|---|---|
| Contenedores de Docker de laboratorios anteriores ocupaban el nombre y el puerto | Existían `postgres-oficioya` y `postgres-parcial` detenidos | Se eliminaron y se creó un contenedor nuevo con `postgres:17` en el puerto 5432 |
| No existía `application-test.properties` | Initializr no crea `src/test/resources` | Se creó la carpeta directamente bajo `src/test` y el archivo con el nombre exacto, para que enlace con `@ActiveProfiles("test")` |
| `New-Item` falló al crear el archivo de pruebas | La carpeta `resources` quedó dentro del paquete `edu/eci/dosw/oficioya` en vez de en `src/test` | Se eliminó y se creó en la ruta correcta |
| `H2` y `h2console` quedaron mal declarados en el `pom.xml` | Initializr los agregó en `runtime` y como dependencia extra | H2 pasó a `scope=test` y se quitó `spring-boot-h2console` |
| `JJWT` no estaba disponible | No existe en Initializr | Se agregaron `jjwt-api`, `jjwt-impl` y `jjwt-jackson` 0.12.6 manualmente |

### Aprendizajes

- Spring Boot 4 separa las dependencias de prueba por módulo (`webmvc-test`, `security-test`, etc.). Si falta `spring-boot-starter-security-test`, las pruebas con `@WithMockUser` responden `401` aunque el código esté bien.
- Los paquetes vacíos no aparecen en Git: se verán en GitHub cuando contengan clases.
- Mientras no exista el login propio, Spring Security imprime una contraseña temporal al arrancar. Es normal y desaparece cuando se implementen los bloques 2 a 4.
- Las variables de entorno definidas con `$env:` en PowerShell solo viven en esa terminal.

### Pendiente para la Fase 1

Con esta base mezclada en `develop`, los cuatro integrantes pueden trabajar en paralelo: registro y login (A), JWT y filtro de seguridad (B), perfiles y administración (C) y solicitudes (D).