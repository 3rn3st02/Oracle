# Estado actual del proyecto ORACLE

Última actualización: 18 de junio de 2026  
Rama de trabajo actual: `android-client`  
Rama estable integrada: `main`  
API cloud activa: `https://api-oraculo.sus.pe/`

---

## 1. Estado general

ORACLE se encuentra en una etapa funcional avanzada.

El sistema ya cuenta con:

- cliente Android operativo,
- UI premium con fondo animado,
- caja inferior tipo chat,
- conexión contra API cloud,
- backend IA RAG integrado,
- contrato API v0.6 soportado,
- documentación técnica,
- evidencias por versión,
- releases APK versionados.

---

## 2. Estado Android

### Estado funcional

La app Android:

- compila correctamente,
- abre sin crashear,
- consume API cloud por HTTPS,
- usa `POST /ask`,
- envía `question`, `language` y `top_k`,
- procesa respuestas desde `data.answer`,
- muestra `related_question` como sugerencia cuando existe,
- ignora `sources` en UI según indicación del backend.

### Estado UI

La app cuenta con:

- fondo animado premium con Canvas,
- nebulosa RGB,
- estrellas y meteoros,
- botón estilizado,
- input inferior tipo chat,
- soporte para teclado,
- soporte para navegación por gestos,
- soporte para navegación por botones inferiores.

---

## 3. Estado Backend

El backend IA RAG fue integrado desde `backend-rag` hacia `main`.

Características actuales:

- FastAPI,
- contrato JSON v0.6,
- respuesta principal dentro de `data`,
- campos `related_question` y `from_cache`,
- servicios de cache,
- servicio de feedback,
- servicio calculadora,
- utilidades de texto,
- Docker y docker-compose.

API principal:

```text
https://api-oraculo.sus.pe/
```

Endpoints relevantes:

```text
GET /health
GET /debug
POST /ask
POST /upload
```

---

## 4. Contrato API actual usado por Android

### Request esperado

```json
{
  "question": "texto de la pregunta",
  "language": "es",
  "top_k": 3
}
```

### Response esperado

```json
{
  "status": "ok",
  "error": null,
  "data": {
    "answer": "texto de la respuesta",
    "related_question": "pregunta sugerida",
    "from_cache": false
  },
  "request_id": "uuid",
  "latency_ms": 399
}
```

### Reglas Android

Android debe mostrar:

```text
data.answer
```

Android debe mostrar como sugerencia si existe:

```text
data.related_question
```

Android no debe mostrar en UI:

```text
sources
```

---

## 5. Estado Git

### Ramas principales

- `main`: estable integrada.
- `android-client`: desarrollo Android.
- `backend-rag`: desarrollo backend IA.

### Estado actual

- `main` y `android-client` quedaron sincronizadas tras el merge estable.
- El último merge integró Android y backend cloud.
- El árbol de trabajo quedó limpio.
- Android compiló correctamente después del merge.
- `backend-rag` se mantiene en worktree paralelo en `/home/ortzadar/Oracle-backend-rag`.

---

## 6. Versiones recientes

### V1.4

Mejora del input inferior tipo chat.

### V1.4.1

Adaptación inicial a nuevo contrato con `data.answer` y request `language/top_k`.

### V1.4.2

Compatibilidad visual con dispositivos Android que usan botones inferiores del sistema.

### V1.4.3

Adaptación final al contrato API cloud v0.6:

- `data.answer`,
- `data.related_question`,
- `from_cache`,
- `sources` ignorado en UI.

---

## 7. Documentación actual

Documentos relevantes:

- `README.md`,
- `docs/setup.md`,
- `docs/troubleshooting.md`,
- `docs/INFORME_TECNICO_CRONOLOGICO.md`,
- `docs/CHANGELOG.md`,
- `docs/evidence/`.

Evidencias por versión:

- `v1.2`,
- `v1.2.1`,
- `v1.3`,
- `v1.3.1`,
- `v1.4`,
- `v1.4.1`,
- `v1.4.2`,
- `v1.4.3`.

---

## 8. Riesgos actuales

Riesgos conocidos:

- cambios frecuentes del contrato JSON del backend,
- límites temporales de tokens/servicio IA,
- endpoints de debug/upload deben protegerse si se exponen en producción,
- evidencias pesadas pueden aumentar tamaño del repo,
- necesidad de mantener sincronizadas `main` y `android-client`.

---

## 9. Próximo paso recomendado

Siguiente fase sugerida:

```text
V1.5 - Chat real con historial de mensajes
```

Objetivo:

- pasar de respuesta única a conversación completa,
- usar burbujas de mensajes,
- scroll automático,
- estado de carga conversacional,
- interacción con sugerencias del backend.

---

## 10. Conclusión

El proyecto se encuentra estable para iniciar la evolución hacia una experiencia conversacional real.

`main` representa una base integrada Android + Backend IA RAG cloud, mientras que `android-client` queda lista para continuar el desarrollo de nuevas funciones de UI.
