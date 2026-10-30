## Bloque 5 · Perfiles y administración (Integrante C)

**Rama:** `feature/profiles-admin`

### Objetivo
Exponer los perfiles de usuario y un listado de administración, cuidando que los datos de contacto (correo y teléfono) solo los vean quienes deben.

### Qué se implementó

| Requisito | Implementación |
|---|---|
| `GET /api/v1/usuarios/me` | Devuelve los datos del usuario autenticado, incluido su contacto. El usuario se identifica por el correo guardado en el token JWT. |
| `GET /api/v1/trabajadores` y `/{id}` | Usan `PerfilPublico`: `id`, `nombre`, `oficioPrincipal`, `zona`. **No** incluyen correo ni teléfono. Solo listan usuarios con rol `TRABAJADOR`. Un id inexistente o de otro rol responde 404. |
| `GET /api/v1/admin/usuarios` | Usa `PerfilAdmin`, que sí incluye `correo` y `telefono`. Protegido por URL en `SecurityConfig` y por `@PreAuthorize("hasRole('ADMIN')")`. |
| ADMIN al arrancar | `AdminSeeder` crea el administrador solo con el perfil `dev`. Es idempotente: si el correo ya existe, no lo duplica. Las credenciales salen de `ADMIN_CORREO` y `ADMIN_PASSWORD`. |

### Componentes nuevos
- DTOs: `PerfilPublico` y `PerfilAdmin`.
- `UsuarioService`, con la lógica de los tres endpoints.
- `UsuarioController`, `TrabajadorController` y `AdminController`.
- `RecursoNoEncontradoException` y su handler en `ApiExceptionHandler` (404 con formato `ApiError`).
- `UsuarioRepository`: nuevos métodos `findByRol` y `findByIdAndRol`.
- `AdminSeeder` (perfil `dev`).

### Decisiones de diseño
- Se usan dos DTOs distintos (público y de administración) para que el contacto nunca salga por el endpoint público.
- Las rutas llevan el prefijo `/api/v1`, igual que `/api/v1/auth` y `/api/v1/admin`.
- La protección del administrador está en dos capas (URL y método), lo que permite el enunciado.

### Pruebas

- Login como ADMIN ---> Invoke-RestMethod guardó la respuesta en $r y el token quedó listo en $hAdmin.
![LoginAdmin](image.png)
![confirmacion de que salen las tres columnas](image-1.png)

- Registro de Trabajor y Contratante
![RegistroTrbaCon](image-2.png)

- endpoints con el token del ADMIN.
Mi perfil: Traer correo y telefono
![Mi perfil: Traer correo y telefono](image-3.png)

Lista de trabajadores: solo Pedro, SIN correo ni telefono
![Lista de trabajadores: solo Pedro, SIN correo ni telefono](image-4.png)

Trabajador por id (el de Pedro, normalmente 2)
![Trabajador por id (el de Pedro, normalmente 2)](image-5.png)

Admin: los 3 usuarios CON correo y telefono
![Admin: los 3 usuarios CON correo y telefono](image-6.png)

- Pruebas durabilidad token

Login de Pedro (trabajador)
![ Login de Pedro (trabajador)](image-7.png)

Trabajador en /admin/usuarios: debe dar 403
![Trabajador en /admin/usuarios: debe dar 403](image-8.png)

Trabajador en su propio perfil:funciona
![Trabajador en su propio perfil:funciona](image-9.png)

Id que no existe: debe dar 404
![Id que no existe: debe dar 404](image-10.png)

Id de Ana, que no es trabajadora: debe dar 404
![Id de Ana, que no es trabajadora: debe dar 404](image-11.png)

Sin token: debe dar 401
![Sin token: debe dar 401](image-12.png)

