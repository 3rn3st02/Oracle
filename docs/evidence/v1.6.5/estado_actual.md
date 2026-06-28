# Estado actual — ORACLE Android v1.6.5

## 1. Resumen ejecutivo

La versión **ORACLE Android v1.6.5** consolida la experiencia visual personalizada iniciada en v1.6.4 y mejora de forma importante la gestión local de conversaciones. Esta versión ataca tres problemas de fondo:

1. Dependencia visual de `Toast` para avisos breves.
2. Drawer mejorable en formato y control de conversaciones.
3. Contaminación del historial por conversaciones vacías creadas demasiado pronto.

La solución aplicada introduce un overlay visual propio ORACLE, mejora el panel lateral y cambia la lógica de creación de conversaciones para que solo nazcan al enviarse una pregunta válida.

---

## 2. Cambios principales de la versión

### 2.1. Overlay ORACLE propio

Se sustituye visualmente el uso de `Toast` en eventos clave por un componente visual propio integrado en la UI.

Características:

- fondo glass mate oscuro;
- borde azul suave;
- texto blanco;
- esquinas redondeadas;
- animación de entrada y salida;
- auto-hide.

Se aplica a:

- pregunta vacía;
- copiar;
- like;
- dislike;
- error de stream.

### 2.2. Overlay local del drawer

Se introduce un segundo overlay específico del panel lateral, usado en esta versión para el mensaje de renombrado de conversación sin cerrar el drawer.

### 2.3. Drawer dinámico mejorado

El drawer ahora usa un título aleatorio personalizado con el nombre del usuario. Además, se mejora la maquetación del texto para evitar cortes visuales feos.

### 2.4. Temarios contraído por defecto

Se elimina la expansión automática inicial de `Temarios`.

### 2.5. Gestión de conversaciones desde el drawer

Se añade long press únicamente en el nodo `Chats`, con menú de acciones:

- Renombrar
- Eliminar

Se implementa selector custom con estilo ORACLE y diálogos coherentes con la estética de la app.

### 2.6. No almacenar chats vacíos

El cambio más importante a nivel de persistencia:

- ya no se crea conversación en Room al arrancar;
- ya no se crea conversación en Room al pulsar “Nuevo chat”;
- la conversación real solo se crea con la primera pregunta válida.

Esto deja el historial limpio y elimina conversaciones fantasma.

---

## 3. Qué no cambia

La v1.6.5 no modifica:

- contrato backend;
- DTOs remotos;
- lógica de streaming;
- estructura de Room;
- glass mate del chat;
- Lottie del input;
- bienvenida efímera v1.6.4;
- feedback local/remoto;
- historial en modo lectura.

---

## 4. Archivos modificados o creados

### Kotlin
- `android/app-oraculo/app/src/main/java/com/oraculo/app/MainActivity.kt`
- `android/app-oraculo/app/src/main/java/com/oraculo/app/ui/chat/OracleWelcomeMessageProvider.kt`
- `android/app-oraculo/app/src/main/java/com/oraculo/app/data/local/chat/repository/ChatLocalRepository.kt`

### Layouts
- `android/app-oraculo/app/src/main/res/layout/activity_main.xml`
- `android/app-oraculo/app/src/main/res/layout/view_oracle_conversation_actions.xml`

### Drawables
- `android/app-oraculo/app/src/main/res/drawable/bg_oracle_overlay_message.xml`
- `android/app-oraculo/app/src/main/res/drawable/bg_oracle_dialog_input.xml`
- `android/app-oraculo/app/src/main/res/drawable/bg_oracle_dialog_option.xml`

---

## 5. Estado funcional actual

### Ya queda resuelto

- avisos con estilo propio ORACLE;
- drawer con mejor legibilidad;
- overlay global y overlay local del drawer funcionando;
- acciones rename/delete accesibles vía long press solo en `Chats`;
- diálogos de acciones con estética ORACLE;
- limpieza del historial de chats vacíos;
- creación perezosa de conversación real con primera pregunta válida;
- eliminación segura de conversación activa y vuelta a estado draft.

### Estado heredado que sigue funcionando

- onboarding y nombre del usuario;
- bienvenida efímera personalizada;
- historial solo lectura;
- feedback;
- copiar;
- burbujas glass mate;
- loading Lottie;
- scroll.

---

## 6. Consideraciones técnicas importantes

### Draft conversation

Se introduce un `draftConversationId` visual que no se guarda en Room. Este id solo existe para mantener la UI de un chat vacío con bienvenida efímera antes de la primera pregunta real.

### Borrado en cascada

No se añade tabla adicional de feedback. El feedback ya vive dentro de `MessageEntity`, por lo que al borrarse los mensajes también desaparece el feedback asociado. Las fuentes se eliminan por `CASCADE` desde `SourceEntity`.

### Riesgo controlado

El bloque más delicado fue `sendQuestion()` y el flujo de `observeConversationInRecycler(...)`, que debían seguir funcionando con draft + conversación real sin romper el stream ni el historial.

---

## 7. Release previsto

- **Rama:** `android-client`
- **Tag:** `APK_1.6.5`
- **Release:** `APK 1.6.5`
- **APK:** `ORACLE_v1.6.5.apk`
- **Staging local:** `/home/ortzadar/Oracle/releases/v1.6.5/`
- **Evidencias:** `/home/ortzadar/Oracle/docs/evidence/v1.6.5/`

---

## 8. Estado final

La v1.6.5 queda lista para:

1. cierre de pruebas;
2. documentación de evidencias;
3. compilación final del APK;
4. commit definitivo;
5. tag y release.
