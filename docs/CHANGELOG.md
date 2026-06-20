# CHANGELOG

## v0.2.5
- Se mantiene estable la base validada de v0.2.4.
- Se añaden respuestas validadas para:
  - Ley de Ohm
  - Funcionamiento interno de un ordenador
  - Fases de búsqueda y de ejecución
  - Fase de interrupción
  - Gestión de interrupciones
  - Técnicas de gestión de interrupciones
  - Organización estructural de un ordenador
  - Explicación de los ocho niveles de la organización estructural de un ordenador
  - Hardware
  - Software
- Se mantiene el comportamiento seguro cuando no hay información suficiente.
- No cambia el contrato del endpoint /ask.


---

## 2026-06-18 - Integración estable Android + Backend RAG cloud

### Resumen

Se integraron en `main` los cambios recientes de `android-client` y `backend-rag`, dejando el repositorio en un estado estable e integrado.

Este merge consolida:

- Cliente Android actualizado.
- Backend IA RAG actualizado.
- Contrato API cloud compatible con Android.
- Documentación y evidencias por versión.
- Eliminación del binario accidental `a.out`.
- Build Android validado correctamente.

### Android

Cambios integrados desde `android-client`:

- Adaptación del cliente Android al contrato API cloud actual.
- Soporte para lectura de respuesta desde `data.answer`.
- Soporte para `related_question` como sugerencia cuando viene en la respuesta.
- Se deja de mostrar `sources` en la UI por indicación del backend.
- Mejora del input inferior tipo chat.
- Compatibilidad visual con dispositivos que usan navegación por gestos o botones inferiores.
- Conservación del fondo animado sin recortes.
- Build validado con `./gradlew assembleDebug`.

### Backend

Cambios integrados desde `backend-rag`:

- Incorporación de backend IA RAG actualizado.
- Nuevos servicios backend:
  - cache_service
  - calculator_service
  - feedback_service
- Mejoras en `rag_service.py`.
- Nuevas utilidades de texto.
- Soporte Docker y docker-compose.
- Archivos de cache y feedback iniciales.
- Actualización de modelos request/response.
- Actualización de `requirements.txt`.
- Incorporación de `docs/CHANGELOG.md`.

### Documentación

Se mantienen actualizadas las siguientes referencias:

- `README.md`
- `docs/setup.md`
- `docs/troubleshooting.md`
- `docs/INFORME_TECNICO_CRONOLOGICO.md`
- Evidencias por versión en `docs/evidence/`

### Validación

Validaciones realizadas:

- `main` y `android-client` quedaron sincronizadas en el commit `b77eb68`.
- `git diff main android-client` no muestra diferencias.
- El APK compila correctamente.
- El árbol de trabajo quedó limpio.
- El binario accidental `a.out` fue eliminado del repo.
- El PDF base del RAG permanece trackeado en `main`.

### Estado

`main` queda como rama estable integrada con Android + Backend IA RAG.

