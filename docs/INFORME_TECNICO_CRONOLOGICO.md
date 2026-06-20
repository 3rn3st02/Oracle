# Informe técnico cronológico - Proyecto ORACLE

Última actualización: 18 de junio de 2026  
Ramas consideradas: `main`, `android-client`, `backend-rag`  
Estado de integración: `main` y `android-client` sincronizadas tras merge de backend y cliente Android.

---

## 1. Resumen ejecutivo

ORACLE es un proyecto compuesto por una aplicación Android desarrollada en Kotlin y un backend de inteligencia artificial RAG desarrollado en Python/FastAPI.

El sistema permite que una persona usuaria realice preguntas desde la app Android y reciba respuestas generadas por una API IA basada en documentos procesados. El proyecto evolucionó desde pruebas locales con backend levantado en PC hasta una integración contra API cloud disponible en:

https://api-oraculo.sus.pe/

Al 18 de junio de 2026, el proyecto cuenta con:

- cliente Android funcional,
- integración contra API cloud,
- contrato JSON actualizado a API v0.6,
- UI con fondo animado y barra inferior tipo chat,
- compatibilidad con navegación por gestos y botones inferiores,
- documentación técnica,
- evidencias por versión,
- releases con APK.

---

## 2. Arquitectura general actual

Flujo funcional actual:

```text
App Android Kotlin
        ↓
HTTPS POST /ask
        ↓
API FastAPI cloud
        ↓
Sistema IA RAG
        ↓
Documentos procesados / contexto / modelo IA
```

API activa:

```text
https://api-oraculo.sus.pe/
```

Endpoint principal Android:

```text
POST /ask
```

Endpoint de salud:

```text
GET /health
```

Endpoint de diagnóstico:

```text
GET /debug
```

---

## 3. Ramas del repositorio

### `main`

Rama estable integrada. Debe contener únicamente estados funcionales y validados.

Estado actual:

- contiene cliente Android actualizado,
- contiene backend IA integrado,
- contiene documentación consolidada,
- contiene evidencias y releases asociados,
- contiene PDF base del RAG versionado para reproducibilidad.

### `android-client`

Rama de trabajo principal de Ernesto.

Responsabilidades de esta rama:

- desarrollo Android,
- UI/UX,
- integración con API,
- adaptación a contratos JSON,
- pruebas desde dispositivo real,
- documentación y evidencias,
- versionado de APK.

Estado actual:

- sincronizada con `main`,
- actualizada hasta los cambios de V1.4.3,
- limpia y sin diferencias frente a `main` tras el merge.

### `backend-rag`

Rama de trabajo de Arandeitors.

Responsabilidades de esta rama:

- backend FastAPI,
- lógica RAG,
- ingestión de documentos,
- scoring y recuperación,
- cache,
- feedback,
- Docker,
- despliegue cloud,
- contrato JSON consumido por Android.

Nota operativa:

La rama `backend-rag` se trabaja en un worktree paralelo en:

```text
/home/ortzadar/Oracle-backend-rag
```

Por eso no se hace checkout directo de `backend-rag` dentro de `/home/ortzadar/Oracle`.

---

## 4. Cronología técnica por fases

### Fase 1 - Base Android y estructura inicial

Periodo aproximado: 10 de junio de 2026

Se creó la base del cliente Android y se estabilizó la configuración inicial.

Cambios relevantes:

- creación de proyecto Android en Kotlin,
- configuración Gradle,
- corrección de manifest,
- app base funcional,
- primeros APKs instalables.

Resultado:

- APK base funcionando.

Versiones relacionadas:

- `APK_V1.0`

---

### Fase 2 - Capa de red Android

Periodo aproximado: 10 al 12 de junio de 2026

Se creó la arquitectura de red del cliente Android.

Archivos y componentes relevantes:

- `OraculoApi.kt`,
- `NetworkConfig.kt`,
- `RetrofitClient.kt`,
- `OraculoRepository.kt`,
- `AskRequest.kt`,
- `AskResponse.kt`,
- `HealthResponse.kt`.

Correcciones realizadas:

- permisos de Internet,
- imports y packages,
- manifest,
- errores de Activity,
- crash por falta de permisos,
- validación de `GET /health` y `POST /ask`.

Versiones relacionadas:

- `APK_V1.1`,
- `APK_V1.1.2`.

---

### Fase 3 - RAG local e ingestión de documentos

Periodo aproximado: 12 al 14 de junio de 2026

Se levantó el backend local y se descubrió que la API respondía sin información útil porque no existían documentos procesados.

Problemas detectados:

- `data/books` sin PDF real,
- `data/processed` sin texto procesado,
- falta de `pypdf`,
- respuestas sin conocimiento suficiente.

Soluciones aplicadas:

- instalación de `pypdf`,
- carga del PDF base,
- ejecución manual de `ingestion_service`,
- generación de texto procesado,
- validación con `curl`,
- ajuste de DTOs para `sources` como objeto.

Resultado:

- la app Android mostró respuestas reales basadas en documentos.

Versiones relacionadas:

- `APK_V1.2`,
- `APK_V1.2.1`.

---

### Fase 4 - UI premium y mejora visual

Periodo aproximado: 14 al 16 de junio de 2026

Se implementó una mejora visual relevante sin modificar la lógica de backend.

Cambios principales:

- `NightChatBackgroundView.kt`,
- fondo animado con Canvas,
- nebulosa RGB,
- estrellas por capas,
- meteoros diagonales,
- botón estilo glass RGB,
- corrección de ProgressBar,
- ciclo de vida de animación con `onPause()` y `onResume()`.

Resultado:

- la app obtuvo identidad visual propia y una experiencia más cercana a producto.

Versión relacionada:

- `APK_V1.3`

---

### Fase 5 - Migración a API cloud

Periodo aproximado: 17 de junio de 2026

Se dejó de usar el flujo local como vía principal y la app pasó a consumir la API desplegada en red.

API usada:

```text
https://api-oraculo.sus.pe/
```

Cambios relevantes:

- `BASE_URL` actualizado a API cloud,
- validación de HTTPS,
- validación de `POST /ask`,
- documentación de transición local → cloud,
- pruebas directas desde Android.

Versión relacionada:

- `APK_V1.3.1`

---

### Fase 6 - Input inferior tipo chat

Periodo aproximado: 18 de junio de 2026

Se empezó la evolución de UI hacia una experiencia tipo chat.

Cambios realizados:

- creación de barra inferior de envío,
- `EditText` expandible según contenido,
- soporte para teclado,
- corrección de insets,
- mejora de accesibilidad táctil,
- preservación del fondo animado.

Problemas resueltos:

- input tapado por teclado,
- referencias incompletas en `MainActivity.kt`,
- `Touch target size too small`,
- `Missing classes` del preview.

Versión relacionada:

- `APK_V1.4`

---

### Fase 7 - Compatibilidad con navegación Android

Fecha: 18 de junio de 2026

Arandeitors probó la app en un dispositivo con navegación por botones inferiores del sistema. Se detectó que la barra inferior quedaba por debajo o demasiado cerca de los controles del sistema.

Cambios realizados:

- ajuste de `WindowInsetsCompat`,
- separación entre altura de teclado y barra de navegación,
- cálculo dinámico de margen inferior,
- compatibilidad con navegación por gestos y botones.

Resultado:

- la barra inferior respeta el teclado y la navegación del sistema.

Versión relacionada:

- `APK_V1.4.2`

---

### Fase 8 - Contrato API cloud v0.6

Fecha: 18 de junio de 2026

Arandeitors actualizó el contrato JSON del backend. Android tuvo que adaptarse nuevamente.

Cambio principal:

Antes Android leía:

```text
response.answer
response.sources
```

Ahora Android debe leer:

```text
response.data.answer
response.data.related_question
```

Campos nuevos del backend:

- `related_question`,
- `from_cache`.

Indicaciones del backend:

- no mostrar `sources` en UI,
- mostrar `data.answer`,
- mostrar `data.related_question` como sugerencia si existe.

Cambios realizados:

- actualización de `AskDataDto.kt`,
- actualización de `AskResponse.kt`,
- actualización de `OraculoRepository.kt`,
- se ignora `sources` en UI,
- se muestra sugerencia cuando existe.

Versión relacionada:

- `APK_V1.4.3`

---

### Fase 9 - Merge estable Android + Backend RAG cloud

Fecha: 18 de junio de 2026

Se integraron los cambios de `android-client` y `backend-rag` en `main`.

Resultado:

- `main` y `android-client` quedaron sincronizadas en el commit `b77eb68`,
- Android compiló correctamente con `./gradlew assembleDebug`,
- se eliminó el binario accidental `a.out`,
- se integraron nuevos servicios backend,
- se añadió soporte Docker del backend,
- se actualizó documentación y evidencias.

Servicios backend incorporados:

- `cache_service.py`,
- `calculator_service.py`,
- `feedback_service.py`,
- `text_utils.py`.

Archivos backend nuevos relevantes:

- `Dockerfile`,
- `docker-compose.yml`,
- `.env.example`,
- `data/cache.json`,
- `data/feedback.json`.

---

## 5. Estado actual del proyecto

Fecha de estado: 18 de junio de 2026

### Android

Estado:

- app compila correctamente,
- UI premium activa,
- input inferior tipo chat,
- compatibilidad con teclado,
- compatibilidad con navegación por botones y gestos,
- consumo de API cloud por HTTPS,
- contrato API v0.6 soportado.

### Backend

Estado:

- backend IA RAG integrado,
- API cloud activa,
- Docker incorporado,
- cache y feedback incorporados,
- contrato JSON actualizado,
- respuesta estructurada dentro de `data`.

### Documentación

Estado:

- README actualizado,
- setup documentado,
- troubleshooting documentado,
- informe técnico cronológico,
- changelog,
- evidencias por versión.

### Git

Estado:

- `main` actualizado,
- `android-client` sincronizada con `main`,
- `backend-rag` se trabaja en worktree paralelo,
- árbol de trabajo limpio,
- PDF base del RAG trackeado,
- `a.out` eliminado.

---

## 6. Recomendaciones siguientes

Siguiente etapa sugerida:

```text
V1.5 - Chat real con historial de mensajes
```

Objetivos sugeridos:

- lista de mensajes,
- burbujas de usuario e IA,
- scroll automático,
- estado “ORACLE está pensando”,
- interacción con `related_question`,
- posibilidad de tocar una sugerencia y enviarla como nueva pregunta.

---

## 7. Conclusión

Al 18 de junio de 2026, ORACLE cuenta con una base Android + Backend IA RAG integrada, validada contra API cloud, con documentación sólida, releases versionados y un flujo de trabajo maduro entre Ernesto y Arandeitors.

El proyecto está listo para evolucionar desde una pantalla de consulta funcional hacia una experiencia conversacional completa.
