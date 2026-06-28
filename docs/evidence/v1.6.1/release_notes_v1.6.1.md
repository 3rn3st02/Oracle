# Release notes — APK 1.6.1

## ORACLE Android v1.6.1

Versión correctiva centrada en mejorar la experiencia del chat durante respuestas largas en streaming.

### Cambios principales

- Corrección del comportamiento de scroll durante respuestas largas.
- Limpieza y reconstrucción de `MainActivity.kt` para eliminar código duplicado y restos de pruebas previas.
- Separación visual de respuestas extensas en bloques más estables.
- Mejora del formato de títulos, subtítulos, elementos numerados y viñetas.
- Corrección del botón copiar para copiar toda la respuesta aunque esté dividida visualmente.
- Ajuste de like/dislike para que sigan aplicándose a la respuesta completa y no a bloques individuales.
- Conservación de feedback real, request_id y sources.

### Evidencias y documentación

- Evidencias visuales: `docs/evidence/v1.6.1/`
- Pruebas: `docs/evidence/v1.6.1/pruebas.md`
- Troubleshooting: `docs/evidence/v1.6.1/troubleshooting.md`
- Estado actual: `docs/evidence/v1.6.1/estado_actual.md`

### Asset del release

Este release debe incluir únicamente el APK como asset.

La documentación y capturas quedan enlazadas desde el repositorio.
