## Bloque 2 · Registro de usuarios (Integrante A)

**Rama:** `feature/auth-registration`

### Objetivo
Permitir el registro de nuevos usuarios en la plataforma aplicando validaciones estrictas de seguridad desde la entrada, cifrando las contraseñas con BCrypt y garantizando un manejo de errores uniforme sin exponer información sensible.

### Qué se implementó

| Requisito | Implementación |
|---|---|
| `POST /api/v1/auth/registro` | Recibe los datos del usuario, los valida y persiste la entidad. Devuelve 201 (Created) con los datos del usuario, omitiendo estrictamente la contraseña y el hash en la respuesta. |
| Validaciones de entrada (400) | Se aplican reglas sobre el DTO: contraseñas de mínimo 8 caracteres, formato de correo válido y rol restringido exclusivamente a `TRABAJADOR` o `CONTRATANTE` mediante expresión regular. |
| Control de duplicidad (409) | Antes de registrar, se normaliza el correo (minúsculas y sin espacios). Si ya existe en la base de datos, se rechaza la petición devolviendo HTTP 409 Conflict. |
| Cifrado de contraseña | Se utiliza `BCryptPasswordEncoder` para generar un *hash* seguro antes de guardar la entidad en la base de datos (RS-02). |
| Uniformidad de errores (RS-09) | Los errores de validación y conflictos se interceptan globalmente para devolver siempre el mismo DTO de error, evitando filtrar trazas de Java. |

### Componentes nuevos
- DTOs: `RegistroRequest`, `UsuarioResponse` y la estructura de error `ApiError`.
- `AuthService`, con la lógica de normalización, verificación de correos y cifrado.
- `AuthController`, exponiendo el endpoint público y disparando las validaciones de Spring (`@Valid`).
- `ApiExceptionHandler` (con `@RestControllerAdvice`), manejando `MethodArgumentNotValidException` y conflictos de lógica de negocio.
- `UsuarioRepository`: nuevo método `existsByCorreo` (y `findByCorreo` para pruebas).
- `SecurityConfig`: creación del *bean* `PasswordEncoder`.

### Decisiones de diseño
- Se implementaron dos DTOs distintos (entrada y salida) actuando como barrera bidireccional; esto garantiza que parámetros como el *hash* de la contraseña jamás se filtren en la respuesta JSON.
- El filtro de roles (evitar la inyección del rol `ADMIN`) se resolvió en la capa REST a través de la etiqueta `@Pattern`, abortando el proceso antes de que los datos toquen la lógica de negocio.
- Centralización total de los errores en un solo controlador de excepciones para cumplir la regla innegociable RS-09, retornando siempre un JSON con `timestamp`, `status`, `error`, `mensaje` y `ruta`.

### Pruebas

- Se desarrollaron y aprobaron las pruebas integrales PT-01, PT-02, PT-03 y PT-04 utilizando MockMvc y el motor H2 en memoria mediante el perfil `test`.
- Se validó manualmente en PostgreSQL que el atributo persistido es un *hash* destructivo (prefijo `$2a$`) y no texto plano.
![CapturaHashBCrypt](docs/evidence/captura_hash.png)