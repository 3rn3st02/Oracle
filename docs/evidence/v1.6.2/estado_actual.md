# Estado actual — ORACLE Android v1.6.2

## Resumen

La versión **ORACLE Android v1.6.2** introduce una mejora visual focalizada en las burbujas del chat. El objetivo es aplicar un estilo **glass mate** a los mensajes de usuario y respuestas de la IA, manteniendo intacta la lógica funcional estabilizada en v1.6.1.

## Alcance de la versión

Esta versión modifica únicamente recursos visuales relacionados con las burbujas de chat.

No se modifican:

- Backend.
- Endpoints.
- DTOs.
- Room.
- Streaming.
- Scroll.
- Feedback.
- Copiar respuesta.
- Historial.
- Sources.
- Drawer.
- Onboarding.

## Cambios implementados

### 1. Glass mate para mensajes de usuario

Se actualiza `bg_chat_user.xml` para aplicar un estilo glass más sobrio:

- Fondo translúcido al 75%.
- Borde mate suave.
- Sombra exterior discreta.
- Reflejo superior reducido.
- Bordes más redondeados.

### 2. Glass mate para respuestas IA

Se actualiza `bg_chat_assistant.xml` para aplicar un estilo glass mate más ligero:

- Fondo translúcido al 50%.
- Borde frío y discreto.
- Brillo superior tenue.
- Sombra inferior suave.
- Bordes más amplios.

### 3. Más espacio interno en burbujas

Se ajustan los `TextView` de usuario y asistente para evitar que el texto quede pegado a los bordes:

- Mayor padding lateral.
- Mayor padding vertical.
- Altura mínima para mensajes de una sola línea.
- Conservación de `wrap_content` y `maxWidth`.

## Archivos modificados

- `android/app-oraculo/app/src/main/res/drawable/bg_chat_user.xml`
- `android/app-oraculo/app/src/main/res/drawable/bg_chat_assistant.xml`
- `android/app-oraculo/app/src/main/res/layout/item_chat_user.xml`
- `android/app-oraculo/app/src/main/res/layout/item_chat_assistant.xml`

## Estado funcional heredado de v1.6.1

Se mantiene operativo:

- Scroll corregido durante streaming.
- División visual de respuestas largas.
- Copiar respuesta completa con `fullContent`.
- Feedback sobre mensaje real con `originalMessageId`.
- Historial en modo lectura.
- Sources solo en historial.
- Nuevo chat.
- Drawer de temarios y conversaciones.
- Onboarding.

## Release previsto

- Rama: `android-client`
- Commit: `V1.4.4 feat(android):glass mate burbujas chat`
- Tag: `APK_1.6.2`
- Release: `APK 1.6.2`
- APK: `ORACLE_v1.6.2.apk`
- Carpeta local APK: `/home/ortzadar/Oracle/releases/v1.6.2/`
- Evidencias: `/home/ortzadar/Oracle/docs/evidence/v1.6.2/`

## Estado final

La versión queda preparada para:

1. Guardar documentos de evidencia.
2. Añadir cambios visuales y docs al commit.
3. Compilar APK debug.
4. Copiar APK a staging local de releases.
5. Crear tag `APK_1.6.2`.
6. Crear release `APK 1.6.2` adjuntando solo `ORACLE_v1.6.2.apk`.
