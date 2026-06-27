# ORACLE – Funcionamiento actual v1.6.0 26062026

## Estado actual de la aplicación
La versión actual de ORACLE mantiene todavía la interfaz principal de pregunta/respuesta, pero ya incorpora la base técnica del futuro modo conversación.

---

## 1. Inicio de la app
### Primer inicio
Si el onboarding no se ha completado:

- se muestra pantalla fullscreen de bienvenida
- aparece fondo Lottie animado
- aparece el ojo Lottie del Oráculo
- se muestra mensaje de bienvenida
- el usuario introduce su nombre
- se valida el nombre
- se muestra advertencia de nombre definitivo
- tras confirmar, el nombre queda guardado y desaparece el onboarding

### Siguientes inicios
Si el onboarding ya se completó:

- el onboarding no vuelve a mostrarse
- la app entra directamente a la pantalla principal

---

## 2. Identidad del usuario
La app mantiene localmente:

- `user_id` persistente por instalación
- `user_name`
- estado de onboarding completado

El nombre no se puede editar desde la app una vez confirmado.

---

## 3. Session actual
Cada vez que la app se abre desde cero:

- se genera una nueva conversación local
- esa conversación tiene un nuevo `session_id`
- ese `session_id` será el que se use para el chat actual

Si la Activity se recrea temporalmente por rotación, la sesión actual se conserva.

---

## 4. Panel lateral
El panel lateral sigue operativo con:

### Temarios
Contiene el árbol jerárquico de unidades:

- Unidad 1
- Unidad 2
- Unidad 3
- Unidad 4
- Unidad 5
- Unidad 6
- Unidad 7

Los nodos funcionan así:

- nodo con hijos → despliega/contrae
- nodo hoja → envía el prompt visible
- `Temarios` actúa como contenedor raíz

---

## 5. Flujo actual de pregunta/respuesta
Actualmente la app sigue usando el flujo visual clásico:

- el usuario escribe una pregunta
- se envía al backend
- la respuesta llega por stream
- la respuesta se muestra en `textAnswer`

Todavía no se representa como burbujas de chat.

---

## 6. Persistencia local preparada
Ya existe la infraestructura local para el futuro modo conversación:

### Base de datos local
- `OracleChatDatabase`

### Entidades
- conversaciones
- mensajes
- fuentes

### Repositorio local
- `ChatLocalRepository`

Esto deja preparada la app para guardar:

- historial de conversaciones
- mensajes del usuario
- respuestas del asistente
- `request_id`
- feedback local
- fuentes asociadas

---

## 7. Rotación de pantalla
Actualmente la app está ajustada para que:

- no pierda la respuesta visible
- no pierda el `session_id` temporal
- el stream no se corte al rotar

---

## 8. Lo que aún NO está implementado
La versión actual todavía no ha terminado de implementar:

- historial de conversaciones visible en el drawer
- modo chat con RecyclerView
- guardar preguntas/respuestas reales en Room durante el stream
- envío real de `user_id` y `session_id` en `/ask/stream`
- captura de `request_id`
- botón copiar
- botón 👍
- botón 👎
- sincronización con `/feedback`

---

## 9. Próxima evolución esperada
La siguiente fase de la app transformará la interfaz actual en un verdadero modo conversación, manteniendo:

- conversaciones múltiples
- historial local
- feedback por mensaje
- integración con contratos backend

## Estado funcional actual de v1.6.0 27062026

La app funciona ya en modo conversación visual principal mediante RecyclerView.

### Flujo actual
- cada arranque completo crea un nuevo `session_id`
- el usuario mantiene `user_id` fijo por instalación
- las preguntas nuevas se envían con:
  - question
  - user_id
  - session_id
- el stream devuelve:
  - tokens
  - request_id final
  - sources finales

### Chat activo
- se muestra en RecyclerView
- permite copiar respuestas
- permite votar 👍 / 👎
- sincroniza feedback con backend

### Historial
- se muestra en RecyclerView
- es de solo lectura
- permite copiar
- muestra la votación ya emitida, sin permitir modificarla
- muestra sources solo en historial
- sources visibles usando solo `label`

### Nuevo chat
- se crea desde botón superior derecho
- genera nuevo `session_id`
- conserva el historial anterior en `Conversaciones`
