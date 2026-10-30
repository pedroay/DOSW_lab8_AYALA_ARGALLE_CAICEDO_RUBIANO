## Bloque 3 · Seguridad con JWT (Integrante B)

*Rama:* `feature/bloque3-jwt`

### Objetivo
Implementar autenticación basada en **JSON Web Tokens (JWT)** para que solo los usuarios autenticados puedan acceder a los recursos protegidos del API. El flujo incluye generación del token al iniciar sesión, validación en cada petición y una respuesta adecuada (401) cuando el token es inválido o falta.

### Qué se implementó

| Requisito | Implementación |
|---|---|
| **POST `/api/v1/auth/login`** | Endpoint en `AuthController` que recibe `LoginRequest`, delega a `AuthService.login` y devuelve `LoginResponse` con el JWT generado. |
| **Generación de JWT** | `JwtService.generar(UserDetails user)` crea un token firmado con HMAC‑SHA256 usando la variable de entorno `JWT_SECRET`. Incluye los claims `sub`, `role`, `iss`, `iat` y `exp`. |
| **Validación de JWT** | `JwtService.validar(String token)` verifica firma, expiración y extrae los claims; devuelve una `Authentication` o lanza excepción si el token es inválido o está expirado. |
| **Filtro de autenticación** | `JwtAuthenticationFilter` extrae el token del encabezado `Authorization: Bearer <token>`, lo valida mediante `JwtService` y coloca la autenticación en el `SecurityContext`. |
| **Manejo de errores 401** | `JsonAuthenticationEntryPoint` devuelve una respuesta **401 Unauthorized** con cuerpo JSON (`ApiError`) cuando el token falta, es corrupto o ha expirado. |
| **Configuración de seguridad** | `SecurityConfig` establece `SessionCreationPolicy.STATELESS`, registra el filtro antes de `UsernamePasswordAuthenticationFilter` y protege todas las rutas excepto `/api/v1/auth/**`. |
| **Pruebas de integración** | `TokenSeguridadTest` verifica: <br>• PT‑07 – login exitoso devuelve token; <br>• PT‑08 – acceso a recurso protegido con token válido devuelve 200; <br>• PT‑09 – acceso sin token o con token inválido/expirado devuelve 401. |
| **Tests unitarios del servicio JWT** | `JwtServiceTest` comprueba generación de claims, detección de token alterado y detección de token expirado. |

### Componentes nuevos
- **Servicio:** `JwtService` (generación y validación).  
- **Servicio de negocio:** `AuthService.login`.  
- **Controlador:** `AuthController` (endpoint login).  
- **Filtro:** `JwtAuthenticationFilter`.  
- **Entry point:** `JsonAuthenticationEntryPoint`.  
- **Configuración:** `SecurityConfig`.  
- **DTOs:** `LoginRequest`, `LoginResponse`.  
- **Tests:** `TokenSeguridadTest`, `JwtServiceTest`.

### Decisiones de diseño
- **Stateless:** La sesión es completamente sin estado; el token lleva toda la información necesaria.  
- **Separación de responsabilidades:** El filtro solo valida el token, mientras que la generación queda encapsulada en `JwtService`.  
- **Manejo centralizado de errores:** `JsonAuthenticationEntryPoint` garantiza respuestas JSON uniformes para errores de autenticación.  
- **Cobertura de pruebas:** Se añadieron pruebas de integración (flujo completo) y unitarias (lógica del token) para asegurar la robustez del mecanismo.

### Pruebas

- **Login → obtención de token**  
  ```powershell
  $r = Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/v1/auth/login `
        -Body @{usuario='user'; clave='pwd'} -ContentType 'application/json'
  $token = $r.token
  ```

- **Acceso a recurso protegido**  
  ```powershell
  $h = @{Authorization = "Bearer $token"}
  Invoke-RestMethod -Method Get -Uri http://localhost:8080/api/v1/solicitudes -Headers $h
  ```

- **Acceso sin token / token inválido** → respuesta `401` con cuerpo JSON:  
  ```json
  {
    "timestamp": "2026-10-30T01:23:45.678+00:00",
    "status": 401,
    "error": "Unauthorized",
    "message": "Invalid or missing JWT",
    "path": "/api/v1/solicitudes"
  }
  ```