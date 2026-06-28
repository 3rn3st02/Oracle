# Troubleshooting — ORACLE Android v1.6.5

## 1. Contexto técnico de la versión

La v1.6.5 se construye sobre una base ya bastante madura, con chat persistido en Room, historial solo lectura, bienvenida efímera, feedback local/remoto, glass mate y Lottie de carga. Los cambios de esta versión no son triviales porque tocan simultáneamente:

- la capa visual de avisos;
- el panel lateral (drawer);
- la interacción sobre conversaciones guardadas;
- el ciclo de vida de creación de conversaciones.

La dificultad no estaba en cada cambio por separado, sino en hacer que todo conviviera sin romper el flujo ya estabilizado en versiones anteriores.

---

## 2. Incidencias y decisiones técnicas relevantes

### 2.1. Sustituir `Toast` por overlay propio sin romper la app

#### Problema

Los avisos del sistema mediante `Toast` funcionaban, pero no estaban alineados visualmente con la estética ORACLE. Sin embargo, reemplazarlos de golpe podía romper:

- timings visuales;
- onboarding;
- interacciones del drawer;
- la sensación de respuesta inmediata.

#### Solución aplicada

Se implementó un overlay propio dentro de `activity_main.xml` para la pantalla principal, con:

- contenedor host;
- `TextView` animado;
- fondo propio `bg_oracle_overlay_message.xml`.

Además se creó una API simple en `MainActivity.kt`:

- `showOracleOverlayMessage(...)`
- `hideOracleOverlayMessage(...)`

#### Resultado

Se pudo sustituir `Toast.makeText(...)` en los eventos relevantes sin alterar la lógica funcional.

---

### 2.2. El overlay no debía interferir con el onboarding

#### Problema

El onboarding está montado como overlay de alta prioridad dentro de la pantalla principal. Si el nuevo overlay ORACLE se mostrara durante el onboarding, competiría visualmente con la experiencia inicial.

#### Solución

En `showOracleOverlayMessage(...)` se añadió la comprobación:

```kotlin
if (::onboardingView.isInitialized && onboardingView.visibility == View.VISIBLE) {
    return
}
```

#### Resultado

El overlay no aparece mientras el onboarding está visible.

---

### 2.3. Temarios desplegado por defecto

#### Problema

El panel `Temarios` se expandía automáticamente en cada arranque debido a esta línea en `onCreate(...)`:

```kotlin
expandedPromptNodes.add(rootPromptTitle)
```

#### Solución

Eliminar la expansión inicial.

#### Resultado

`Temarios` queda contraído por defecto y el usuario decide cuándo expandirlo.

---

### 2.4. Selector nativo de acciones visualmente pobre

#### Problema

El `AlertDialog.setItems(...)` nativo para `Renombrar / Eliminar` no encajaba con el estilo ORACLE. Además, el usuario pidió explícitamente que el selector tuviera el mismo lenguaje visual del overlay.

#### Solución

Se creó un layout custom:

```text
view_oracle_conversation_actions.xml
```

con dos opciones visuales:

- `actionRenameConversation`
- `actionDeleteConversation`

ambas usando:

```text
bg_oracle_dialog_option.xml
```

#### Resultado

El menú de long press dejó de verse genérico y pasó a integrarse con la identidad visual ORACLE.

---

### 2.5. Overlay de renombrado quedaba oculto detrás del drawer

#### Problema

El mensaje de “La conversación ha recibido un nuevo nombre.” debía verse con el drawer aún abierto. El overlay global no era suficiente porque estaba pensado para la pantalla principal y no para el panel lateral en primer plano.

#### Primera tentación incorrecta

Mover el overlay global fuera de `main` para ponerlo por encima del `DrawerLayout`. Esta opción tenía riesgo real de romper interacciones previas del drawer y el onboarding.

#### Decisión correcta

No tocar el overlay global.

En su lugar, crear un **overlay local del drawer**:

- `drawerOverlayMessageContainer`
- `drawerOverlayMessageText`

insertado dentro de `drawerContent`.

#### Resultado

- rename mantiene el drawer abierto;
- el mensaje aparece encima del panel lateral;
- delete sigue usando el overlay global porque el drawer se cierra.

---

### 2.6. Diálogo de renombrado sin coherencia visual

#### Problema

El diálogo de rename necesitaba:

- fondo ORACLE;
- input ORACLE;
- botones coherentes;
- no parecer una ventana genérica del sistema.

#### Solución

Se creó:

```text
bg_oracle_dialog_input.xml
```

Y se añadió helper:

```kotlin
styleOracleDialog(dialog, input)
```

que aplica:

- fondo del diálogo con `bg_oracle_overlay_message`;
- coloración de botones;
- input estilizado.

#### Resultado

El diálogo de renombrado quedó visualmente consistente con el overlay y con el resto de ORACLE.

---

### 2.7. Riesgo más delicado: chats vacíos en Room

#### Problema real

Hasta esta versión, la app persistía conversaciones demasiado pronto:

- al arrancar,
- al pulsar “Nuevo chat”.

Eso ensuciaba el historial con conversaciones vacías.

#### Puntos implicados

- `startNewConversationForAppLaunch()`
- `startNewChatFromDrawer()`
- `sendQuestion()`
- `currentSessionId`
- bienvenida efímera
- observación del chat activo

#### Solución aplicada

Se adoptó un estado intermedio visual de chat borrador usando:

```kotlin
private val draftConversationId = "__draft_conversation__"
```

Este draft:

- no se guarda en Room;
- solo existe para sostener la UI inicial;
- mantiene bienvenida efímera;
- no genera historial sucio.

La conversación persistida solo se crea al enviar la primera pregunta válida.

#### Resultado

Se elimina el problema de los chats vacíos sin sacrificar la UX del “chat listo para empezar”.

---

### 2.8. Error de compilación y referencias rotas en `sendQuestion()`

#### Síntoma

Aparecieron errores como:

- `Expecting an element`
- `Unresolved reference 'streamResult'`
- `Unresolved reference 'assistantMessageId'`
- `Unresolved reference 'receivedRequestId'`

#### Causa

Durante la refactorización para crear conversación real solo con la primera pregunta válida, quedó cortado un bloque intermedio de `sendQuestion()`. En concreto:

- apareció un `)` suelto;
- desapareció el bloque donde se declaraban:
  - `receivedRequestId`
  - `receivedSources`
  - `assistantMessage`
  - `assistantMessageId`
  - `streamResult`

#### Solución

Reinsertar correctamente todo el bloque entre:

```kotlin
val sessionId = ...
```

y

```kotlin
streamResult.onFailure { ... }
```

#### Lección

Cuando se modifica el flujo interno de `sendQuestion()`, es preferible reemplazar bloques completos y no intercalar líneas manualmente.

---

### 2.9. Conservar historial limpio al borrar conversación activa

#### Problema

Si el usuario borra la conversación actualmente abierta, la UI puede quedar apuntando a una conversación inexistente.

#### Solución

Dentro de `deleteConversationFromDrawer(...)` se detecta si la conversación borrada era:

- la visible actualmente;
- la sesión persistida actual.

Y entonces:

- se cancela observación;
- se limpia estado visible;
- se pone `currentSessionId = null` si corresponde;
- se reconstruye estado draft con bienvenida efímera.

#### Resultado

No se rompe el flujo tras borrar una conversación abierta.

---

## 3. Validaciones estructurales clave

### 3.1. Overlay global

Debe estar correctamente enlazado en:

- `activity_main.xml`
- `bindViews()`
- `showOracleOverlayMessage(...)`

### 3.2. Overlay local del drawer

Debe existir dentro de:

```xml
<LinearLayout android:id="@+id/drawerContent">
```

No debe sustituir ni borrar `drawerTitle`.

### 3.3. Long press solo en Chats

El `setOnLongClickListener` debe vivir únicamente en:

```kotlin
renderConversationsSection()
```

No en `renderPromptNode(...)`.

### 3.4. Draft conversation

Debe existir:

```kotlin
private val draftConversationId = "__draft_conversation__"
```

Y el flujo draft debe pasar por:

- `startNewConversationForAppLaunch()`
- `startNewChatFromDrawer()`
- `showDraftConversationState()`

### 3.5. Persistencia real

La conversación real debe crearse solo dentro de `sendQuestion()` cuando la pregunta ya fue validada como no vacía.

---

## 4. Confirmaciones importantes de datos locales

### 4.1. Feedback

No existe tabla separada de feedback.

El feedback está embebido en `MessageEntity` mediante:

- `feedbackState`
- `feedbackUseful`
- `feedbackSynced`

### 4.2. Borrado completo

Gracias a las claves foráneas:

- `ConversationEntity` → `MessageEntity` con `CASCADE`
- `MessageEntity` → `SourceEntity` con `CASCADE`

por tanto al borrar una conversación:

- se borran mensajes;
- se borran fuentes;
- el feedback desaparece con los mensajes.

---

## 5. Estado final del troubleshooting

La v1.6.5 toca varias capas a la vez: UI, drawer, acciones de conversación y ciclo de persistencia. Los puntos más delicados fueron:

- overlay sobre drawer;
- reemplazo de selector nativo por selector custom;
- evitar chats vacíos sin romper bienvenida efímera;
- reconstruir correctamente `sendQuestion()`.

La estrategia que mejor funcionó fue:

- separar overlay global y overlay local del drawer;
- mantener `Temarios` sin cambios de interacción;
- añadir gestión de conversaciones solo en `Chats`;
- introducir estado draft para UX sin contaminación de Room.

Con ello, la v1.6.5 queda funcionalmente más limpia, visualmente más consistente y con un historial mucho más sano.
