# Pruebas — ORACLE Android v1.6.2

## Objetivo

Validar la versión **ORACLE Android v1.6.2**, centrada exclusivamente en una mejora visual del chat: aplicación de estilo **glass mate** en las burbujas de mensajes de usuario y respuestas de la IA.

Esta versión no modifica backend, Room, streaming, feedback, historial, sources ni lógica de scroll.

## Entorno de prueba

- Rama: `android-client`
- Versión objetivo: `v1.6.2`
- Tag previsto: `APK_1.6.2`
- Release previsto: `APK 1.6.2`
- APK previsto: `ORACLE_v1.6.2.apk`
- Proyecto Android: `/home/ortzadar/Oracle/android/app-oraculo`
- Staging local del APK: `/home/ortzadar/Oracle/releases/v1.6.2/`
- Evidencias visuales: `/home/ortzadar/Oracle/docs/evidence/v1.6.2/`

## Cambios bajo prueba

### Archivos visuales modificados

- `android/app-oraculo/app/src/main/res/drawable/bg_chat_user.xml`
- `android/app-oraculo/app/src/main/res/drawable/bg_chat_assistant.xml`
- `android/app-oraculo/app/src/main/res/layout/item_chat_user.xml`
- `android/app-oraculo/app/src/main/res/layout/item_chat_assistant.xml`

## Casos de prueba

### 1. Compilación de recursos Android

**Pasos**

1. Ir al proyecto Android:

```bash
cd /home/ortzadar/Oracle/android/app-oraculo
```

2. Ejecutar compilación debug:

```bash
./gradlew assembleDebug
```

**Resultado esperado**

- La tarea `assembleDebug` finaliza correctamente.
- No aparecen errores de XML en `bg_chat_user.xml` ni `bg_chat_assistant.xml`.
- No aparecen errores del tipo `XMLStreamException` o declaración XML duplicada.

**Estado**

- Validado tras corregir los drawables glass mate.

---

### 2. Visualización de mensaje de usuario

**Pasos**

1. Abrir la app en dispositivo real.
2. Enviar un mensaje corto de una sola línea.
3. Observar la burbuja del mensaje de usuario.

**Resultado esperado**

- La burbuja del usuario se muestra con estilo glass mate.
- El texto no queda pegado a los bordes.
- La burbuja conserva espacio vertical suficiente en mensajes de una sola línea.
- La opacidad base del usuario se mantiene en 75%.

**Estado**

- Pendiente de validar con captura en `docs/evidence/v1.6.2/`.

---

### 3. Visualización de respuesta IA

**Pasos**

1. Enviar una pregunta que genere respuesta corta.
2. Observar la burbuja de respuesta de la IA.

**Resultado esperado**

- La burbuja de la IA se muestra con estilo glass mate.
- El efecto es más sobrio que el glass brillante inicial.
- El texto tiene espacio lateral y vertical suficiente.
- La opacidad base de la IA se mantiene en 50%.

**Estado**

- Pendiente de validar con captura en `docs/evidence/v1.6.2/`.

---

### 4. Respuesta larga con bloques visuales

**Pasos**

1. Enviar una pregunta que genere respuesta larga.
2. Observar varios bloques de respuesta.
3. Revisar que el estilo glass se aplique a todos los bloques.

**Resultado esperado**

- Todos los bloques de respuesta mantienen el estilo glass mate.
- No hay recortes visuales en los bordes.
- El texto no se sale de la burbuja.
- El formato de títulos, subtítulos, numeración y viñetas sigue funcionando como en v1.6.1.

**Estado**

- Pendiente de validar con capturas.

---

### 5. Scroll durante streaming

**Pasos**

1. Enviar una pregunta larga.
2. Observar el comportamiento del scroll automático.
3. Interrumpir manualmente el scroll.
4. Volver al final.

**Resultado esperado**

- El comportamiento del scroll se mantiene igual que en v1.6.1.
- La mejora visual no introduce regresiones.
- El chat sigue permitiendo lectura manual y reenganche al final.

**Estado**

- Sin cambios funcionales intencionados.

---

### 6. Copiar, like y dislike

**Pasos**

1. Generar una respuesta.
2. Pulsar copiar.
3. Pulsar 👍 o 👎.

**Resultado esperado**

- Copiar sigue copiando toda la respuesta.
- Like/dislike siguen aplicándose a la respuesta completa.
- El cambio visual no altera feedback ni `request_id`.

**Estado**

- Sin cambios funcionales intencionados.

## Evidencias

Las capturas deben guardarse en:

```text
/home/ortzadar/Oracle/docs/evidence/v1.6.2/
```

El release debe enlazar esta carpeta, pero no adjuntar los documentos ni las capturas como assets.
