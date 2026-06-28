# Pruebas — ORACLE Android v1.6.4

## 1. Objetivo de la versión

La versión **ORACLE Android v1.6.4** da una función real al nombre introducido durante el onboarding. Hasta esta versión, el nombre se validaba y se guardaba localmente, pero no tenía un uso visible dentro de la experiencia de la aplicación. A partir de v1.6.4, el nombre se utiliza para personalizar la experiencia del chat y del panel lateral.

La versión se centra en cinco bloques funcionales:

1. **Bienvenida efímera en el chat activo** usando el nombre del usuario.
2. **Nuevo tipo visual** `ChatUiItem.WelcomeMessage`.
3. **Mensajes dinámicos personalizados** para eventos clave:
   - pregunta vacía,
   - copiar respuesta,
   - like,
   - dislike,
   - error de stream.
4. **Título del drawer personalizado** con el nombre del usuario.
5. **Sin persistencia en Room** de la bienvenida efímera.

---

## 2. Alcance de validación

### Se debe validar que la versión:

- use correctamente el nombre guardado en onboarding;
- no rompa la conversación activa;
- no rompa el historial;
- no altere el flujo de streaming;
- no altere la persistencia en Room;
- mantenga la experiencia visual previa del aviso inferior;
- mantenga el comportamiento corregido del scroll heredado de v1.6.1;
- mantenga las burbujas glass mate heredadas de v1.6.2;
- mantenga la animación Lottie de carga heredada de v1.6.3.

---

## 3. Entorno de prueba

- **Repo:** `https://github.com/3rn3st02/Oracle`
- **Rama de trabajo:** `android-client`
- **Tag previsto:** `APK_1.6.4`
- **Release previsto:** `APK 1.6.4`
- **APK previsto:** `ORACLE_v1.6.4.apk`
- **Proyecto Android:** `/home/ortzadar/Oracle/android/app-oraculo`
- **Carpeta de staging local del APK:** `/home/ortzadar/Oracle/releases/v1.6.4/`
- **Evidencias:** `/home/ortzadar/Oracle/docs/evidence/v1.6.4/`

---

## 4. Archivos funcionalmente relevantes en esta versión

- `android/app-oraculo/app/src/main/java/com/oraculo/app/MainActivity.kt`
- `android/app-oraculo/app/src/main/java/com/oraculo/app/ui/chat/ChatAdapter.kt`
- `android/app-oraculo/app/src/main/java/com/oraculo/app/ui/chat/OracleWelcomeMessageProvider.kt`
- `android/app-oraculo/app/src/main/java/com/oraculo/app/ui/chat/models/ChatUiItem.kt`

---

## 5. Casos de prueba

### Caso 1 — La app arranca sin onboarding pendiente

**Precondición**
- Ya existe nombre guardado en `UserIdentityStore`.
- `isOnboardingCompleted()` devuelve `true`.

**Pasos**
1. Abrir la aplicación.
2. Esperar a que se construya la conversación inicial.
3. Observar el RecyclerView del chat.

**Resultado esperado**
- Se crea una conversación local nueva.
- Se muestra una **bienvenida efímera** al principio del chat.
- La bienvenida contiene el nombre del usuario.
- La bienvenida usa una frase aleatoria del proveedor.
- La bienvenida no muestra copiar / like / dislike.

**Resultado observado esperado para cierre de versión**
- Validado en dispositivo real.

---

### Caso 2 — La bienvenida no se guarda en historial

**Pasos**
1. Abrir la app.
2. Confirmar que aparece la bienvenida efímera.
3. Crear o enviar varios mensajes reales.
4. Volver al drawer.
5. Abrir una conversación guardada en modo historial.

**Resultado esperado**
- El historial muestra únicamente mensajes persistidos de Room.
- La bienvenida efímera **no aparece** en la conversación histórica.
- El historial sigue funcionando con modo lectura.

---

### Caso 3 — Nuevo chat desde botón superior

**Pasos**
1. Pulsar el botón de nuevo chat.
2. Verificar que se crea `session_id` nuevo.
3. Observar el estado inicial del RecyclerView.

**Resultado esperado**
- Se limpia la conversación activa anterior.
- Se genera una nueva bienvenida efímera para el nuevo chat.
- La bienvenida usa el nombre del usuario.
- No se guarda en Room.

---

### Caso 4 — Pregunta vacía

**Pasos**
1. Abrir la app.
2. No escribir nada en `editQuestion`.
3. Pulsar enviar.

**Resultado esperado**
- No se envía ninguna petición al backend.
- Se muestra el aviso inferior con el **mismo estilo visual** actual.
- El texto usa el nombre del usuario y una frase aleatoria del grupo de mensajes vacíos.

**Ejemplo esperado**
- `Álvaro, no hay respuestas correctas para preguntas equivocadas.`

---

### Caso 5 — Copiar respuesta

**Pasos**
1. Generar una respuesta real del asistente.
2. Pulsar copiar en el último bloque.
3. Pegar el contenido en un campo de texto externo.

**Resultado esperado**
- Se copia toda la respuesta (`fullContent`).
- El aviso inferior mantiene el mismo estilo visual actual.
- El texto del aviso está personalizado con el nombre del usuario.

---

### Caso 6 — Like

**Pasos**
1. Generar una respuesta.
2. Pulsar 👍.

**Resultado esperado**
- Se guarda feedback local.
- Si existe `request_id`, se intenta sincronizar con backend.
- Se muestra aviso inferior con el nombre del usuario.
- El mensaje corresponde al grupo de feedback positivo.

---

### Caso 7 — Dislike

**Pasos**
1. Generar una respuesta.
2. Pulsar 👎.

**Resultado esperado**
- Se guarda feedback local.
- Si existe `request_id`, se intenta sincronizar con backend.
- Se muestra aviso inferior con el nombre del usuario.
- El mensaje corresponde al grupo de feedback negativo.

---

### Caso 8 — Error de stream

**Pasos**
1. Forzar fallo del backend, o probar cuando el backend no responda.
2. Enviar una pregunta.

**Resultado esperado**
- La respuesta visible usa un mensaje de error personalizado con el nombre del usuario.
- Se puede seguir incluyendo `Detalle: ...` para diagnóstico.
- El flujo de la app no se rompe.

---

### Caso 9 — Drawer personalizado

**Pasos**
1. Abrir la app con onboarding ya completado.
2. Abrir el drawer.

**Resultado esperado**
- El título superior del drawer ya no muestra solo `ORACLE`.
- El texto aparece como:
  - `Oráculo invocado por Álvaro`
- Si el usuario acaba de confirmar su nombre, el título se actualiza en caliente.

---

### Caso 10 — No regresión de funciones anteriores

**Verificar que siguen funcionando**
- onboarding;
- guardado del nombre;
- nuevo chat;
- historial;
- scroll durante streaming;
- burbujas glass mate;
- carga Lottie sobre el input;
- copiar;
- like/dislike;
- sources solo en historial.

---

## 6. Resultado esperado de validación final

La versión puede darse por válida si:

- la bienvenida efímera aparece en conversación activa;
- no aparece en historial;
- los mensajes personalizados funcionan en vacío, copiar, like, dislike y error;
- el drawer muestra el nombre del usuario;
- no se producen regresiones visuales ni funcionales.
