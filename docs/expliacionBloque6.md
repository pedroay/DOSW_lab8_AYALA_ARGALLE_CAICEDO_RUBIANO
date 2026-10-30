### Bloque 6: Corte Vertical de Solicitudes y Control de Acceso por Propiedad

#### Objetivo
Implementar los endpoints, lógica de negocio y máquina de estados para las solicitudes de servicio en OficioYa, garantizando el aislamiento de datos por propiedad (RS-07) y la autorización estricta por roles (RS-08).

#### Cambios Realizados
1. **Seguridad y Extracción de Identidad del Contexto:**
   - La identidad del usuario autenticado se extrae exclusivamente del objeto `Authentication` (`auth.getName()`), impidiendo la inyección o suplantación de identidades en el body o URL.
   - **Aislamiento por propiedad:** Las consultas se filtran en base de datos mediante `findByIdAndContratanteId`, `findByIdAndTrabajadorId` o `findByIdAndParticipante`.
   - **Prevención de fuga de información:** Toda consulta o intento de modificación sobre una solicitud ajena responde **`404 Not Found`** (idéntico a si no existiera), evitando revelar la existencia del recurso a terceros.

2. **Endpoints y Control de Acceso por Rol (`SolicitudController`):**
   - `POST /api/v1/solicitudes`: Exclusivo para `CONTRATANTE` (`@PreAuthorize("hasRole('CONTRATANTE')")`). Valida que el trabajador exista con dicho rol y asigna el estado inicial `PENDIENTE` (`201 Created`).
   - `GET /api/v1/solicitudes`: Permitido para `CONTRATANTE` y `TRABAJADOR`. Lista únicamente las solicitudes donde el usuario autenticado participa.
   - `GET /api/v1/solicitudes/{id}`: Acceso restringido exclusivamente a las dos partes involucradas en la solicitud (`200 OK` / `404 Not Found`).
   - `PATCH /api/v1/solicitudes/{id}/aceptar`: Exclusivo para el `TRABAJADOR` asignado.
   - `PATCH /api/v1/solicitudes/{id}/rechazar`: Exclusivo para el `TRABAJADOR` asignado.
   - `PATCH /api/v1/solicitudes/{id}/cancelar`: Exclusivo para el `CONTRATANTE` autor.

3. **Máquina de Estados y Transiciones Permitidas (`SolicitudService`):**
   - Transiciones legales implementadas:
     - `PENDIENTE` ➔ `ACEPTADA` (Trabajador asignado).
     - `PENDIENTE` ➔ `RECHAZADA` (Trabajador asignado).
     - `PENDIENTE` o `ACEPTADA` ➔ `CANCELADA` (Contratante autor).
   - Cualquier intento de transición no permitida lanza `EstadoInvalidoException`.

4. **DTOs y Manejo de Errores:**
   - `SolicitudRequestDto`: Validación de campos de entrada con Jakarta (`@NotNull`, `@NotBlank`).
   - `SolicitudResponseDto`: Payload de respuesta que oculta datos sensibles de contacto.
   - `EstadoInvalidoException` y registro en `ApiExceptionHandler` retornando HTTP **`409 Conflict`** con el formato estándar `ApiError`.

5. **Pruebas de Seguridad (`SolicitudSeguridadTest`):**
   - **PT-13:** `TRABAJADOR` intenta crear solicitud -> `403 Forbidden`.
   - **PT-14:** Contratante lista solicitudes -> Solo devuelve las creadas por él.
   - **PT-15:** Contratante consulta o cancela solicitud ajena -> `404 Not Found` (sin alteraciones en base de datos).
   - **PT-16:** Aceptar con trabajador no asignado (`404`) y con contratante (`403`).
   - **PT-17:** `ADMIN` intenta acceder a `/solicitudes` -> `403 Forbidden`.
   - Casos límite de transiciones válidas y rechazo de transiciones inválidas con `409 Conflict`.

#### Verificación
- Comando de ejecución: `./mvnw test -Dtest=SolicitudSeguridadTest`
- 10 de 10 pruebas exitosas (0 fallos, 0 errores).