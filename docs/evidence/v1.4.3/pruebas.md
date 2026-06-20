# Pruebas V1.4.3 - Adaptación a API cloud v0.6

## Objetivo

Validar que el cliente Android ORACLE se adapte correctamente al nuevo contrato JSON del backend IA cloud v0.6.

---

## Contexto

Arandeitors informó un cambio de contrato en la API.

La respuesta ahora incluye los datos principales dentro del objeto:

data

Esto implica que Android ya no debe leer:

response.answer

sino:

response.data.answer

---

## Cambio principal del backend

Antes Android esperaba una respuesta similar a:

{
  "answer": "...",
  "sources": [],
  "status": "ok",
  "error": null,
  "request_id": "...",
  "latency_ms": 291
}

Ahora la API responde con una estructura similar a:

{
  "status": "ok",
  "error": null,
  "data": {
    "answer": "texto de la respuesta",
    "sources": [],
    "related_question": "pregunta sugerida",
    "from_cache": false
  },
  "request_id": "uuid",
  "latency_ms": 399
}

---

## Cambios aplicados en Android

### 1. AskDataDto

Se actualiza el modelo para soportar:

- answer
- related_question
- from_cache

El campo sources se ignora en la UI por indicación del backend.

---

### 2. AskResponse.kt

Se adapta la respuesta principal para leer el objeto data.

---

### 3. OraculoRepository.kt

Se actualiza el flujo de lectura de respuesta.

Antes se leía:

body.answer  
body.sources

Ahora se lee:

body.data?.answer  
body.data?.related_question

---

## Cambio en la UI

La app muestra:

- respuesta principal desde data.answer
- sugerencia desde data.related_question si existe

La app ya no muestra sources en pantalla.

---

## Validación

Se validó que:

- la app compila correctamente
- la app usa POST /ask
- la app consulta https://api-oraculo.sus.pe/
- Android consume data.answer correctamente
- Android muestra related_question como sugerencia cuando existe
- sources deja de mostrarse en la UI

---

## Resultado

La app queda compatible con el contrato actual de la API cloud v0.6.

---

## Conclusión

V1.4.3 corrige compatibilidad con backend cloud v0.6 y deja el cliente Android preparado para seguir evolucionando la experiencia conversacional.
