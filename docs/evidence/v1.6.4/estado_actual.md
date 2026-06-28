# Estado actual — ORACLE Android v1.6.4

## 1. Resumen ejecutivo

La versión **ORACLE Android v1.6.4** convierte el nombre introducido durante el onboarding en un elemento funcional real de la experiencia del usuario.

Hasta v1.6.3, el nombre se validaba, se confirmaba y se guardaba, pero no producía un efecto visible persistente en la navegación ni en el flujo del chat. En v1.6.4 se añade una capa de personalización que afecta a la experiencia visual del chat activo, del drawer y de varios mensajes de interacción breve.

---

## 2. Qué cambia en esta versión

### 2.1. Bienvenida efímera en el chat activo

Se introduce un nuevo tipo visual:

```kotlin
ChatUiItem.WelcomeMessage
```

Este tipo representa una burbuja efímera de ORACLE que:

- se muestra al inicio de la app,
- se muestra al crear un nuevo chat,
- utiliza el nombre del usuario,
- no se guarda en Room,
- no aparece en historial,
- no tiene acciones.

### 2.2. Provider centralizado de mensajes personalizados

Se crea:

```text
OracleWelcomeMessageProvider.kt
```

Este provider centraliza textos aleatorios y personalizados para:

- bienvenida;
- pregunta vacía;
- primer mensaje sugerido;
- error de stream;
- feedback positivo;
- feedback negativo;
- copiar respuesta;
- título del drawer.

### 2.3. Drawer con título personalizado

El encabezado del drawer ya no es estático. Ahora refleja el nombre del usuario:

```text
Oráculo invocado por Álvaro
```

### 2.4. Avisos inferiores personalizados

Se mantiene el mecanismo visual actual para los avisos inferiores (Toast/estilo actual), pero el contenido textual pasa a ser dinámico y usa el nombre del usuario.

Aplicado a:

- pregunta vacía;
- copiar;
- like;
- dislike;
- error.

---

## 3. Qué no cambia en esta versión

La v1.6.4 **no modifica**:

- backend;
- endpoints;
- contrato JSON;
- Room;
- estructura de mensajes reales;
- streaming SSE;
- scroll corregido en v1.6.1;
- glass mate de burbujas de v1.6.2;
- Lottie de carga sobre input en v1.6.3;
- modo lectura del historial;
- sources solo en historial.

---

## 4. Archivos afectados

### Afectados directamente

- `android/app-oraculo/app/src/main/java/com/oraculo/app/MainActivity.kt`
- `android/app-oraculo/app/src/main/java/com/oraculo/app/ui/chat/ChatAdapter.kt`
- `android/app-oraculo/app/src/main/java/com/oraculo/app/ui/chat/OracleWelcomeMessageProvider.kt`
- `android/app-oraculo/app/src/main/java/com/oraculo/app/ui/chat/models/ChatUiItem.kt`

### Afectados indirectamente por uso

- `android/app-oraculo/app/src/main/java/com/oraculo/app/data/local/preferences/UserIdentityStore.kt`

---

## 5. Estado funcional heredado

La app mantiene:

- onboarding con confirmación del nombre;
- conversación activa con Room;
- mensajes USER / ASSISTANT persistidos;
- historial solo lectura;
- feedback local y remoto;
- copiar respuesta completa;
- división visual de respuestas largas;
- drawer de conversaciones y temarios;
- fondo animado;
- Lottie de carga sobre el input;
- releases por APK.

---

## 6. Estado actual de la versión

### Lo que ya queda resuelto

- El nombre del onboarding ya tiene uso visible real.
- La bienvenida efímera funciona en conversación activa.
- La bienvenida no contamina el historial.
- El provider centralizado evita hardcodear frases en múltiples puntos.
- El drawer refleja el nombre del usuario.
- Los mensajes inferiores personalizados ya usan el nombre del usuario.

### Riesgos técnicos controlados

- Se corrigió el fallo donde el chat activo dejaba de pintar mensajes reales por una mezcla incorrecta de listas visuales en `observeConversationInRecycler(...)`.
- Se mantuvo la observación desde Room y se añadió la bienvenida como item visual externo a Room.
- El historial sigue funcionando porque `openConversationReadOnly(...)` usa `ChatUiMapper.mapMessagesToUiItems(messages)` sin mezclar la bienvenida efímera.

---

## 7. Release previsto

- **Rama:** `android-client`
- **Tag:** `APK_1.6.4`
- **Release:** `APK 1.6.4`
- **APK:** `ORACLE_v1.6.4.apk`
- **Staging local APK:** `/home/ortzadar/Oracle/releases/v1.6.4/`
- **Evidencias:** `/home/ortzadar/Oracle/docs/evidence/v1.6.4/`

---

## 8. Estado final

La versión queda lista para:

1. documentar evidencias;
2. compilar APK debug;
3. subir commit a `android-client`;
4. crear tag `APK_1.6.4`;
5. crear release `APK 1.6.4` con solo el APK como asset.
