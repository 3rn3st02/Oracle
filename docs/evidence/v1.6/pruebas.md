# ORACLE – Pruebas v1.6.0

## Objetivo de esta versión
La versión **v1.6.0** introduce la base del nuevo modo conversación, onboarding inicial del usuario y persistencia local para futuras conversaciones múltiples.

---

## Cambios implementados

### 1. Onboarding inicial
Se implementó un onboarding fullscreen con:

- animación principal del ojo (Lottie)
- fondo animado del onboarding (Lottie)
- mensaje de bienvenida del Oráculo
- campo de texto para introducir nombre
- botón de aceptación del nombre
- validación de nombre
- confirmación obligatoria antes de guardar el nombre

### 2. Identidad local del usuario
Se añadió persistencia local de identidad mediante `UserIdentityStore`, que actualmente conserva:

- `user_id` persistente por instalación
- `user_name`
- estado de onboarding completado

### 3. Validación del nombre
Se aplicaron reglas de validación:

- máximo 10 caracteres
- alfanumérico
- permite tildes
- permite `-`, `_` y `@`
- no permite vacío
- no permite solo espacios
- no permite caracteres no contemplados

### 4. Confirmación irreversible del nombre
Antes de guardar el nombre, la app muestra una advertencia informando que el nombre quedará asociado al Oráculo de ese dispositivo y no podrá editarse más adelante desde la app.

### 5. Integración visual del onboarding
Se integró el onboarding dentro del layout principal de la Activity y se corrigió su comportamiento para:

- quedar visualmente por encima de toda la interfaz
- bloquear clics sobre elementos de fondo
- ocultar el botón del drawer mientras el onboarding está visible

### 6. Base local para modo conversación
Se configuró Room y se creó la estructura base para conversaciones locales.

Se añadieron:

#### Modelos
- `AskStreamMetadata`
- `ChatConversation`
- `ChatMessage`
- `ChatRole`
- `ChatSource`
- `FeedbackState`

#### Entidades Room
- `ConversationEntity`
- `MessageEntity`
- `SourceEntity`

#### DAO
- `ConversationDao`
- `MessageDao`
- `SourceDao`

#### Base de datos
- `OracleChatDatabase`

#### Repositorio local
- `ChatLocalRepository`

### 7. Session por arranque de aplicación
Se definió que cada vez que la app se inicia desde cero:

- se crea un nuevo `session_id`
- el `user_id` permanece
- la conversación activa es nueva
- el historial antiguo quedará disponible más adelante en el listado de conversaciones

### 8. Comportamiento ante rotación
Se aplicaron mejoras para que al rotar el dispositivo:

- no se pierda la respuesta visible
- no se pierda el `session_id` temporal actual
- el stream no se corte por recreación de `MainActivity`

---

## Validaciones realizadas

### Onboarding
-  Aparece solo cuando el onboarding no ha sido completado
-  Se oculta tras guardar nombre válido
-  No vuelve a mostrarse en reinicios normales
-  El botón lateral no interfiere durante el onboarding

### Nombre de usuario
-  Se rechaza nombre vacío
-  Se rechaza nombre con solo espacios
-  Se rechaza longitud > 10
-  Se aceptan letras, números, tildes, `-`, `_`, `@`

### Drawer / Temarios
-  El árbol de `Temarios` vuelve a desplegar correctamente
-  El onboarding ya no rompe el panel lateral
-  El botón del drawer se restaura al cerrar onboarding

### Persistencia local
-  `user_id` se conserva entre ejecuciones
-  `user_name` se conserva entre ejecuciones
-  Room compila y la base local queda inicializada
-  El repositorio local se inicializa en `MainActivity`

### Rotación
-  Se conserva el estado visual básico
-  El stream ya no se corta por rotación

---

## Estado funcional actual

La app actualmente tiene:

- onboarding funcional
- persistencia de identidad local
- base local Room preparada
- `session_id` nuevo por arranque
- estructura del panel lateral intacta
- temarios operativos
- base preparada para historial, feedback y chat

Todavía no se ha migrado la respuesta visual actual al historial real tipo chat.

---

## Próximos pasos previstos
A partir de este punto, la siguiente fase consistirá en:

1. adaptar `/ask/stream` para enviar `question`, `user_id` y `session_id`
2. capturar `request_id` del stream
3. guardar preguntas y respuestas reales en Room
4. construir lista `Conversaciones` en el drawer
5. migrar la UI al modo chat
6. añadir copiar / 👍 / 👎 por respuesta
7. conectar `/feedback`

