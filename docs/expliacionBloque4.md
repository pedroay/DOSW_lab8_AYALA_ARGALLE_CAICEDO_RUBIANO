### Bloque 4: Configuración de Spring Security y Filtro de Autenticación JWT

#### Objetivo
Proteger la API REST mediante una arquitectura *Stateless* con validación de tokens JWT en cada petición entrante y unificar el manejo de errores de autenticación y autorización (401 y 403).

#### Cambios Realizados
1. **Filtro de Autenticación (`JwtAuthenticationFilter`):**
   - Implementado como `OncePerRequestFilter`.
   - Intercepta el encabezado `Authorization: Bearer <token>`.
   - Valida firma, expiración y emisor mediante `JwtService`.
   - Extrae el claim `role` y establece en el `SecurityContextHolder` la autoridad con prefijo `ROLE_` (`ROLE_TRABAJADOR`, `ROLE_CONTRATANTE`, `ROLE_ADMIN`).
   - Si el token está alterado, vencido o ausente, limpia el contexto para delegar la respuesta al punto de entrada.

2. **Manejadores Uniformes de Error (401 y 403):**
   - **`JsonAuthenticationEntryPoint` (401 Unauthorized):** Responde con código `401` y el JSON unificado `ApiError` ante peticiones sin token, tokens manipulados o vencidos (`"Token inválido o expirado"`).
   - **`JsonAccessDeniedHandler` (403 Forbidden):** Responde con código `403` y el formato `ApiError` cuando un usuario autenticado intenta acceder a un recurso sin los privilegios requeridos (ej. no ADMIN en `/api/v1/admin/**`).

3. **Actualización de `SecurityConfig`:**
   - Inyección de `JwtAuthenticationFilter`, `AuthenticationEntryPoint` y `AccessDeniedHandler`.
   - Registro del filtro JWT antes de `UsernamePasswordAuthenticationFilter`.
   - Configuración de `exceptionHandling` para redirigir 401 y 403 a los manejadores JSON personalizados.
   - Sesiones *Stateless*, CSRF deshabilitado y control de acceso por URL (`/api/v1/auth/**` público, `/api/v1/admin/**` solo ADMIN, resto autenticado).

4. **Pruebas de Seguridad (`TokenSeguridadTest`):**
   - **PT-07:** Petición a recurso protegido sin token -> `401 Unauthorized`.
   - **PT-08:** Petición con firma de token alterada -> `401 Unauthorized`.
   - **PT-09:** Petición con token expirado -> `401 Unauthorized`.
   - **403:** Intento de acceso a `/api/v1/admin/**` con rol no administrativo -> `403 Forbidden`.

#### Verificación
- Pruebas del bloque ejecutadas y superadas al 100% con H2 (`./mvnw test`).