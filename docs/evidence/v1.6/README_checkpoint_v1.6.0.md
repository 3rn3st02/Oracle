# ORACLE — Checkpoint v1.6.0

## Estado actual
La versión **v1.6.0** introduce la base del nuevo modo conversación de ORACLE sin romper el flujo actual de pregunta/respuesta.

Actualmente la app ya incorpora:

- onboarding fullscreen con identidad local del usuario
- persistencia de `user_id` y `user_name`
- validación y confirmación irreversible del nombre
- panel lateral `Temarios` funcionando correctamente
- base local Room preparada para conversaciones, mensajes, fuentes y feedback
- creación de un nuevo `session_id` por cada arranque completo de la app
- continuidad del stream al rotar el dispositivo

---

## Qué hay implementado en este checkpoint

### Onboarding
- Pantalla inicial fullscreen con animación del ojo y fondo animado.
- Solicitud del nombre del usuario en el primer inicio.
- Validación del nombre:
  - máximo 10 caracteres
  - alfanumérico
  - permite tildes, `-`, `_` y `@`
- Confirmación obligatoria antes de guardar el nombre.
- El botón lateral del drawer no interfiere durante el onboarding.

### Identidad local
- `user_id` persistente por instalación.
- `user_name` persistente tras completar onboarding.
- Cada arranque completo de la app crea un **nuevo `session_id`** para el chat activo.

### Base local
Se ha configurado Room y ya existen:

- modelos de chat
- entidades Room
- DAO
- `OracleChatDatabase`
- `ChatLocalRepository`

Esto deja preparada la persistencia local para:

- conversaciones múltiples
- mensajes del usuario y del asistente
- `request_id` de respuestas streaming
- feedback local
- fuentes asociadas a respuestas

### Panel lateral
- `Temarios` sigue funcionando como árbol jerárquico.
- Las unidades 1–7 están integradas.
- El onboarding ya no rompe la interacción del drawer.

---

## Qué falta a partir de este punto
A partir del checkpoint **v1.6.0**, los siguientes pasos serán:

1. adaptar la capa de red para enviar `question + user_id + session_id` en `/ask/stream`
2. capturar y guardar `request_id` del evento final del stream
3. guardar preguntas y respuestas reales en Room
4. añadir el bloque `Conversaciones` en el panel lateral
5. migrar la interfaz a modo chat
6. añadir acciones por respuesta:
   - copiar
   - 👍
   - 👎
7. conectar feedback real con `POST /feedback` usando `request_id`

---

## Estado del checkpoint
✅ Compilación correcta  
✅ Onboarding estable  
✅ Drawer funcional  
✅ Room integrada  
✅ Base conversacional preparada  
🔄 Pendiente de integrar el stream con `user_id/session_id/request_id` real

