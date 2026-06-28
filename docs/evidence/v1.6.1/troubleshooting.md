# Troubleshooting — ORACLE Android v1.6.1

## Contexto de la versión

La versión **v1.6.1** se centra exclusivamente en corregir el comportamiento del chat Android durante respuestas largas en streaming y en mejorar el formato visual del texto recibido desde el backend. La base funcional heredada de v1.6.0 se mantiene intacta: onboarding, identidad local, Room, historial, feedback, fuentes en historial, nuevo chat y drawer.

## Problema principal corregido

### Síntoma observado

Durante respuestas largas recibidas por streaming:

- Si el usuario no tocaba la pantalla, el scroll automático seguía la respuesta durante un tramo.
- Si el usuario tocaba o interrumpía el scroll, el RecyclerView podía quedarse anclado en una posición intermedia.
- En respuestas largas, el scroll no permitía bajar hasta el estado actual del stream hasta que la respuesta terminaba.
- Al pulsar feedback en respuestas largas, en algunos estados previos el RecyclerView podía saltar al inicio del bloque de respuesta.

### Causa probable

El problema estaba relacionado con la combinación de:

- actualizaciones frecuentes del mensaje ASSISTANT durante streaming;
- un único item visual demasiado grande en RecyclerView;
- refrescos/rebinds mientras el contenido crecía;
- lógica de autoscroll que podía desactivarse por movimientos programáticos o por cálculos de distancia al fondo.

## Corrección aplicada

### 1. Limpieza de MainActivity

Se reconstruyó `MainActivity.kt` para eliminar código duplicado, bloques corruptos y restos de pruebas anteriores relacionados con throttling de Room.

Se eliminaron restos como:

```kotlin
currentStreamingContent
lastStreamingRoomUpdateAt
lastStreamingRoomSavedContent
minStreamingRoomUpdateIntervalMs
minStreamingRoomUpdateChars
```

Estos restos habían quedado mezclados dentro/fuera de `repository.askStream(...)` y podían romper compilación o comportamiento.

### 2. Scroll durante streaming

Se dejó una política más estable:

- Cada nueva pregunta reactiva `shouldAutoScrollChat = true`.
- El scroll automático baja al último bloque visual disponible.
- Se aplica refuerzo hacia el fondo real del RecyclerView con `scrollRecyclerChatToRealBottom()`.
- El listener de scroll solo modifica `shouldAutoScrollChat` cuando detecta gesto manual real del usuario.
- Los movimientos programáticos del RecyclerView ya no desactivan accidentalmente el seguimiento automático.

### 3. Separación visual de respuestas largas

El texto del asistente sigue guardándose como un único mensaje real en Room, pero se divide visualmente en bloques estables mediante `ChatUiMapper`.

Esto evita que una respuesta larga sea un único item gigante en RecyclerView.

### 4. Copiar respuesta completa

Al dividir una respuesta en bloques visuales, el botón copiar inicialmente copiaba solo el último bloque. Se corrigió añadiendo:

- `fullContent`: respuesta completa limpia;
- `originalMessageId`: id real del mensaje en Room;
- `showsActions`: indica qué bloque debe mostrar copiar/feedback.

Ahora el botón copiar aparece solo en el último bloque visual, pero copia toda la respuesta.

### 5. Feedback sobre respuesta completa

Like/dislike siguen aplicándose a la respuesta completa, no a bloques individuales.

El callback usa `originalMessageId`, no el id visual del bloque.

## Formato de texto

Se adaptó el formateo para convertir respuestas del backend como:

```text
**Título**
*Subtítulo*
1.
Elemento
: descripción
* punto
```

En una visualización más limpia:

```text
Título destacado
Subtítulo destacado
1. Elemento: descripción
• punto
```

### Reglas actuales

- `**texto**` → título visual.
- `*texto*` → subtítulo visual.
- `1. Elemento: descripción` → elemento numerado reconstruido.
- `* punto` o `*punto` → viñeta limpia.
- Copiar elimina los marcadores Markdown visibles.

## Verificaciones recomendadas si reaparece el fallo

### Revisar MainActivity

Comprobar que `repository.askStream(...)` no contiene bloques sueltos fuera de `onToken` / `onDone`.

### Revisar RecyclerView

Comprobar que:

```kotlin
recyclerChat.itemAnimator = null
recyclerChat.setHasFixedSize(false)
```

siguen presentes.

### Revisar ChatAdapter

Comprobar que copiar usa `fullContent` y feedback usa `originalMessageId`.

### Revisar ChatUiMapper

Comprobar que los bloques visuales tienen ids estables:

```text
messageId_block_0
messageId_block_1
messageId_block_2
```

## Estado final del troubleshooting

- El scroll automático vuelve a funcionar durante respuestas nuevas.
- Las respuestas largas se dividen visualmente.
- Copiar copia toda la respuesta.
- Like/dislike siguen afectando a la respuesta completa.
- El resto de funcionalidades de v1.6.0 no se han modificado intencionadamente.
