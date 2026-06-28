# Estado actual — ORACLE Android v1.6.1

## Resumen ejecutivo

La versión **ORACLE Android v1.6.1** es una versión correctiva sobre la base estable de v1.6.0. El objetivo principal ha sido corregir el comportamiento del scroll durante respuestas largas en streaming y mejorar la presentación visual del texto recibido desde el backend.

No se han introducido cambios funcionales de alcance amplio fuera del bug tratado.

## Estado funcional actual

### Se mantiene desde v1.6.0

- Onboarding inicial con nombre de usuario.
- Persistencia local de identidad.
- Conversaciones locales con Room.
- Nuevo chat con nuevo `session_id`.
- Historial local en modo lectura.
- Drawer con conversaciones y temarios.
- Chat principal con RecyclerView.
- Feedback real con `/feedback`.
- `request_id` asociado a respuestas del asistente.
- Sources visibles solo en historial.
- Health backend funcional.

### Cambios incorporados en v1.6.1

#### 1. Corrección de MainActivity

Se reconstruyó y limpió `MainActivity.kt` para eliminar código duplicado y restos de pruebas anteriores que afectaban al flujo de streaming y scroll.

La versión corregida conserva:

- envío a `/ask/stream`;
- actualización de Room durante el stream;
- finalización con `request_id`;
- guardado de sources;
- feedback local y remoto;
- navegación de historial.

#### 2. Scroll durante streaming

Se ajustó la lógica de seguimiento automático para respuestas largas:

- cada nueva pregunta reactiva `shouldAutoScrollChat = true`;
- el RecyclerView baja al último bloque visual disponible;
- se refuerza el desplazamiento al fondo real;
- el listener distingue entre gesto manual y scroll programático;
- el scroll programático ya no desactiva por sí mismo el seguimiento automático.

#### 3. Separación visual de respuestas largas

Las respuestas largas del asistente ya no dependen de un único item gigante en RecyclerView. El mensaje real sigue siendo uno en Room, pero visualmente se divide en bloques estables.

Esto mejora:

- legibilidad;
- estabilidad visual;
- interacción con copiar y feedback;
- comportamiento durante respuestas extensas.

#### 4. Formato de texto recibido

Se adaptó el formateo de texto para mejorar la visualización:

- títulos;
- subtítulos;
- elementos numerados;
- viñetas con `*`;
- limpieza de marcadores Markdown visibles.

El texto copiado también se limpia para que sea más legible.

#### 5. Copiar respuesta completa

Al estar la respuesta dividida visualmente, se añadió soporte para que el botón copiar siga copiando toda la respuesta mediante `fullContent`.

#### 6. Feedback sobre mensaje real

Los botones 👍 / 👎 siguen apareciendo solo al final de la respuesta, pero el voto se aplica al mensaje real de Room usando `originalMessageId`.

## Archivos relevantes modificados

- `app/src/main/java/com/oraculo/app/MainActivity.kt`
- `app/src/main/java/com/oraculo/app/ui/chat/models/ChatUiItem.kt`
- `app/src/main/java/com/oraculo/app/ui/chat/models/ChatUiMapper.kt`
- `app/src/main/java/com/oraculo/app/ui/chat/ChatAdapter.kt`

## Decisiones funcionales

### Botones en respuestas divididas

Los botones no aparecen por cada bloque.

Regla actual:

- Copiar: solo en el último bloque, copia toda la respuesta.
- Like: solo en el último bloque, vota toda la respuesta.
- Dislike: solo en el último bloque, vota toda la respuesta.
- Fuentes: solo en el último bloque dentro de historial.

### Room y backend

No se modifica el contrato backend.

La división en bloques es solo visual. La respuesta sigue siendo un único mensaje real en Room.

## Release previsto

- Tag: `APK_1.6.1`
- Release: `APK 1.6.1`
- Asset único: APK debug
- Documentación enlazada mediante links al repo
- Carpeta de evidencias: `docs/evidence/v1.6.1/`

## Estado final

La versión queda lista para:

1. Compilar APK debug.
2. Subir cambios a la rama `android-client`.
3. Crear tag `APK_1.6.1`.
4. Crear release `APK 1.6.1` con el APK como único asset.
5. Enlazar documentación y evidencias desde la descripción del release.
