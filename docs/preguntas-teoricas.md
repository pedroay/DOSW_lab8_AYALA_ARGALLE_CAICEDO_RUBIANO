
### 1. ¿Dónde aparecen en su proyecto codificar, cifrar y hashear? ¿Por qué la contraseña se hashea y no se cifra?
* **Codificar (Encoding):** Aparece en la serialización Base64URL del header y payload del JWT. No proporciona confidencialidad ni seguridad, solo representa datos binarios en texto transportable por HTTP.
* **Cifrar (Encryption):** Implica un proceso bidireccional con clave para ocultar información que luego debe ser descifrada (por ejemplo, comunicación HTTPS/TLS en tránsito).
* **Hashear (Hashing):** Aparece en el almacenamiento de contraseñas mediante **BCrypt** (`PasswordEncoder`) y en la firma HMAC-SHA256 del JWT.
* **¿Por qué la contraseña se hashea y no se cifra?:** La contraseña debe ser un proceso estrictamente unidireccional e irreversible. Si una contraseña se cifrara, existiría una clave de descifrado que, de ser comprometida, expondría las contraseñas en texto plano de todos los usuarios. Con hashing con sal (salt) y costo computacional (BCrypt), el sistema verifica la autenticidad comparando los hashes (`passwordEncoder.matches`) sin necesidad de conocer ni poder recuperar la contraseña original.

---

### 2. Decodificaron el payload de su token: ¿qué contiene, qué pasó cuando cambiaron el `role` a `ADMIN` y por qué el servidor lo detectó?
* **Contenido del Payload:** Contiene los claims públicos `sub` (correo del usuario), `role` (rol asignado, ej. `TRABAJADOR` o `CONTRATANTE`), `iss` (`oficioya`), `iat` (fecha de emisión) y `exp` (fecha de expiración a 30 minutos). No contiene contraseñas, teléfonos ni información sensible (Regla 7).
* **¿Qué pasó al cambiar el `role` a `ADMIN`?:** El servidor rechazó la petición retornando `401 Unauthorized`.
* **¿Por qué lo detectó?:** Porque la firma digital del JWT se genera aplicando HMAC-SHA256 sobre `Base64URL(Header) + "." + Base64URL(Payload)` utilizando la clave secreta `JWT_SECRET` conocida únicamente por el servidor. Al alterar un solo carácter del payload (cambiar el rol a `ADMIN`), el hash recalculado por el servidor ya no coincide con la firma adjunta en el token, invalidando la petición por alteración de firma (`SignatureException`).

---

### 3. Den un ejemplo de `401`, `403` y `404` de su API. ¿Por qué una solicitud ajena responde `404` y no `403`?
* **Ejemplo 401 Unauthorized:** Una petición a `/api/v1/usuarios/me` sin cabecera `Authorization`, o con un token vencido o con firma alterada. El cliente no está autenticado o su token no es válido.
* **Ejemplo 403 Forbidden:** Un usuario autenticado con rol `TRABAJADOR` o `CONTRATANTE` intentando consultar `/api/v1/admin/usuarios`, o un `TRABAJADOR` intentando crear una solicitud (`POST /api/v1/solicitudes`). El servidor sabe quién es el usuario, pero su rol no posee los permisos requeridos.
* **Ejemplo 404 Not Found:** Un contratante intentando consultar o cancelar una solicitud con id `5` que pertenece a otro contratante (`GET /api/v1/solicitudes/5`).
* **¿Por qué responde `404` y no `403`?:** Responder `403` confirmaría a un atacante que el recurso con id `5` efectivamente existe en el sistema (ataque de enumeración de recursos). Responder `404` hace indistinguible una solicitud inexistente de una solicitud ajena, protegiendo la confidencialidad de la información.

---

### 4. ¿Por qué el contratante no envía su propio ID al crear una solicitud?
Porque la identidad del usuario debe derivarse **exclusivamente del contexto de seguridad autenticado** (`SecurityContextHolder` / token JWT) y nunca del cuerpo de la petición ni de parámetros en la URL (Regla de seguridad obligatoria 2). Si el ID viniera en el payload JSON, un contratante malintencionado autenticado podría suplantar a otro contratante enviando el ID de su víctima, violando el principio de no repudio y rompiendo el control de acceso.

---

### 5. ¿Por qué desactivaron CSRF? ¿Qué limitación tiene el cierre de sesión sin revocación de tokens?
* **¿Por qué se desactivó CSRF?:** Porque la arquitectura del API es **stateless** (sin estado de sesión en servidor). Las peticiones no dependen de cookies de sesión gestionadas automáticamente por el navegador, sino de cabeceras explícitas `Authorization: Bearer <token>`. Como los navegadores no adjuntan cabeceras personalizadas de forma implícita entre orígenes cruzados, los ataques CSRF (Cross-Site Request Forgery) no son aplicables.
* **Limitación del cierre de sesión sin revocación de tokens:** En un esquema JWT puro sin estado, un token emitido sigue siendo criptográficamente válido hasta que expira su claim `exp` (30 minutos en OficioYa). Si el usuario "cierra sesión" eliminando el token únicamente en el cliente, si el token fue interceptado previamente por un tercero, este podrá continuar utilizándolo hasta su expiración, a menos que se implemente una lista de revocación (blacklist en caché como Redis) o tokens de corta duración con Refresh Tokens.

---

### 6. ¿Qué pasaría si la clave del JWT se sube a GitHub, o si es corta? ¿Qué cambiarían para llevar esta API a producción?
* **Clave subida a GitHub o corta:**
  * Si la clave se expone públicamente, cualquier atacante puede firmar sus propios tokens arbitrarios con cualquier rol (incluyendo `ADMIN`) y suplantar a cualquier usuario del sistema, anulando por completo la seguridad de la API.
  * Si la clave es corta (menos de 256 bits), es vulnerable a ataques de fuerza bruta offline para descifrar el secreto a partir de un token capturado.
* **Cambios para llevar esta API a producción:**
  1. Utilizar un gestor de secretos profesional (AWS Secrets Manager, Azure Key Vault, HashiCorp Vault) para inyectar `JWT_SECRET` y credenciales de base de datos en tiempo de despliegue.
  2. Implementar cifrado asimétrico (RS256 o EdDSA) con par de claves pública/privada para que los servicios que validan no requieran la clave que firma.
  3. Establecer rotación periódica de claves de firma.
  4. Implementar tokens de acceso de corta duración (5-10 minutos) con Refresh Tokens almacenados de forma segura con revocación activa.
  5. Configurar HTTPS estricto con HSTS, Rate Limiting contra ataques de denegación de servicio y fuerza bruta, y Web Application Firewall (WAF).