# Pruebas — ORACLE Android v1.6.5

## 1. Objetivo de la versión

La versión **ORACLE Android v1.6.5** refina de forma importante la experiencia visual y la gestión local de conversaciones. Esta versión se centra en dos grandes áreas:

1. **Experiencia visual y UX**
   - Sustitución práctica de los avisos del sistema por un **overlay propio ORACLE**.
   - Mejora del **drawer lateral** con variantes dinámicas de título y mejor maquetación.
   - **Temarios cerrado por defecto** al iniciar la app.
   - **Selector de acciones de conversación** con estilo visual ORACLE.
   - **Diálogo de renombrado** y confirmación de borrado con estética coherente.

2. **Persistencia y limpieza del historial**
   - **No almacenar chats vacíos**.
   - Crear conversación persistida **solo cuando existe la primera pregunta válida**.
   - Mantener el estado draft visual del chat sin contaminar Room.
   - Borrado completo de conversación, mensajes y fuentes asociadas.

---

## 2. Alcance exacto de validación

Se debe validar que la v1.6.5:

- muestre el overlay propio ORACLE en los avisos ya personalizados;
- mantenga el estilo visual coherente con ORACLE;
- no muestre overlay encima del onboarding;
- muestre correctamente el overlay local del drawer durante renombrado;
- mantenga el overlay global para delete y otros avisos;
- deje `Temarios` contraído por defecto;
- permita renombrar una conversación desde `Chats` mediante long press;
- permita eliminar una conversación desde `Chats` mediante long press;
- elimine también los mensajes y fuentes asociados;
- no genere chats vacíos en Room al iniciar ni al pulsar “Nuevo chat”; 
- cree conversación persistida solo con la primera pregunta válida;
- no rompa historial, streaming, glass, Lottie ni feedback.

---

## 3. Entorno de prueba

- **Repo:** `https://github.com/3rn3st02/Oracle`
- **Rama:** `android-client`
- **Tag previsto:** `APK_1.6.5`
- **Release previsto:** `APK 1.6.5`
- **APK previsto:** `ORACLE_v1.6.5.apk`
- **Proyecto Android:** `/home/ortzadar/Oracle/android/app-oraculo`
- **Carpeta local release:** `/home/ortzadar/Oracle/releases/v1.6.5/`
- **Evidencias:** `/home/ortzadar/Oracle/docs/evidence/v1.6.5/`

---

## 4. Archivos relevantes de la versión

### UI y experiencia visual
- `android/app-oraculo/app/src/main/res/layout/activity_main.xml`
- `android/app-oraculo/app/src/main/res/drawable/bg_oracle_overlay_message.xml`
- `android/app-oraculo/app/src/main/res/drawable/bg_oracle_dialog_input.xml`
- `android/app-oraculo/app/src/main/res/drawable/bg_oracle_dialog_option.xml`
- `android/app-oraculo/app/src/main/res/layout/view_oracle_conversation_actions.xml`

### Lógica principal
- `android/app-oraculo/app/src/main/java/com/oraculo/app/MainActivity.kt`
- `android/app-oraculo/app/src/main/java/com/oraculo/app/ui/chat/OracleWelcomeMessageProvider.kt`

### Persistencia / conversaciones
- `android/app-oraculo/app/src/main/java/com/oraculo/app/data/local/chat/repository/ChatLocalRepository.kt`
- `android/app-oraculo/app/src/main/java/com/oraculo/app/data/local/chat/db/dao/ConversationDao.kt`
- `android/app-oraculo/app/src/main/java/com/oraculo/app/data/local/chat/db/dao/MessageDao.kt`
- `android/app-oraculo/app/src/main/java/com/oraculo/app/data/local/chat/db/dao/SourceDao.kt`

---

## 5. Casos de prueba funcionales

### Caso 1 — Overlay ORACLE en pregunta vacía

**Pasos**
1. Abrir la app.
2. No escribir nada en el input.
3. Pulsar enviar.

**Resultado esperado**
- No se envía ninguna petición.
- No se crea conversación persistida.
- Se muestra el overlay ORACLE con mensaje personalizado.
- No aparece un `Toast` del sistema.

---

### Caso 2 — Overlay ORACLE en copiar

**Pasos**
1. Generar una respuesta real.
2. Pulsar copiar.

**Resultado esperado**
- Se copia el contenido completo.
- Se muestra el overlay ORACLE global.
- El texto usa el nombre del usuario.

---

### Caso 3 — Overlay ORACLE en like/dislike

**Pasos**
1. Generar una respuesta.
2. Pulsar 👍.
3. Generar otra respuesta.
4. Pulsar 👎.

**Resultado esperado**
- Se guarda feedback local correctamente.
- Se intenta sincronización si hay `request_id`.
- Se muestra overlay ORACLE global en ambos casos.

---

### Caso 4 — Overlay ORACLE en error

**Pasos**
1. Forzar una caída del backend o fallo del stream.
2. Enviar una pregunta.

**Resultado esperado**
- Se muestra overlay ORACLE con mensaje de error personalizado.
- `textAnswer` sigue mostrando detalle técnico.

---

### Caso 5 — Drawer dinámico

**Pasos**
1. Abrir la app con onboarding completado.
2. Abrir el drawer.

**Resultado esperado**
- El título del drawer muestra una variante aleatoria con el nombre del usuario.
- El texto está bien maquetado y no rompe visualmente el panel.

---

### Caso 6 — Temarios cerrado por defecto

**Pasos**
1. Abrir la app.
2. Abrir el drawer.

**Resultado esperado**
- `Temarios` aparece contraído.
- El usuario debe pulsarlo para expandirlo.

---

### Caso 7 — Long press en Chats

**Pasos**
1. Abrir el drawer.
2. Hacer pulsación larga sobre una conversación dentro de `Chats`.

**Resultado esperado**
- Se abre selector ORACLE con:
  - `Renombrar`
  - `Eliminar`
- Esto no aplica a `Temarios`.

---

### Caso 8 — Renombrar conversación

**Pasos**
1. Long press en un chat.
2. Pulsar `Renombrar`.
3. Escribir nuevo nombre.
4. Guardar.

**Resultado esperado**
- El diálogo usa estilo ORACLE.
- El input usa fondo ORACLE.
- El drawer permanece abierto.
- Se muestra overlay local del drawer con confirmación.
- El nombre se actualiza en `Chats`.
- Si la conversación estaba abierta en modo historial, se actualiza su cabecera.

---

### Caso 9 — Eliminar conversación

**Pasos**
1. Long press en un chat.
2. Pulsar `Eliminar`.
3. Confirmar.

**Resultado esperado**
- Se elimina la conversación.
- Se eliminan también mensajes y fuentes por cascada.
- El feedback desaparece al ir embebido en `MessageEntity`.
- Se cierra el drawer.
- Se muestra overlay global ORACLE.

---

### Caso 10 — Eliminar conversación actualmente abierta

**Pasos**
1. Abrir una conversación histórica.
2. Abrir drawer.
3. Long press sobre esa misma conversación.
4. Eliminar.

**Resultado esperado**
- Se cancela la observación actual.
- La UI vuelve a estado draft.
- No se crea conversación vacía nueva.
- El usuario puede seguir usando la app.

---

### Caso 11 — No almacenar chats vacíos al iniciar

**Pasos**
1. Abrir la app.
2. No escribir ninguna pregunta.
3. Abrir drawer.

**Resultado esperado**
- No aparece una conversación nueva vacía en `Chats`.
- Solo se muestra draft visual con bienvenida efímera.

---

### Caso 12 — No almacenar chats vacíos al pulsar Nuevo chat

**Pasos**
1. Pulsar botón de nuevo chat.
2. No escribir ninguna pregunta.
3. Abrir drawer.

**Resultado esperado**
- No aparece una nueva conversación vacía en `Chats`.

---

### Caso 13 — Crear conversación solo con primera pregunta válida

**Pasos**
1. Estar en estado draft.
2. Escribir una pregunta real.
3. Enviar.

**Resultado esperado**
- En ese momento se crea conversación persistida.
- Se guarda el mensaje USER.
- Se crea el mensaje streaming ASSISTANT.
- El drawer ya muestra la conversación real.

---

## 6. No regresión

Validar que siguen bien:

- onboarding;
- bienvenida efímera v1.6.4;
- historial solo lectura;
- burbujas glass mate;
- Lottie de carga sobre input;
- copiar;
- like/dislike;
- scroll durante streaming;
- sources en historial.

---

## 7. Criterio de cierre

La v1.6.5 puede considerarse válida si:

- el overlay propio sustituye correctamente a los avisos clave;
- el drawer se ve mejor y funciona como se espera;
- rename/delete funcionan sin romper flujo;
- no se generan chats vacíos persistidos;
- el historial y las conversaciones reales siguen funcionando.
