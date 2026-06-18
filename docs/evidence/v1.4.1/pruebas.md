# Pruebas V1.4.1 - Adaptación al nuevo contrato API cloud

## Versión

1.4.1

---

## Contexto
cambio del backend,  breaking change real del contrato JSON, cliente Android debe adaptarse.
Antes Android esperaba algo así:
{
  "answer": "...",
  "sources": [],
  "status": "ok",
  "error": null,
  "request_id": "...",
  "latency_ms": 291
}

Ahora la API devuelve algo tipo:
{
  "data": {
    "answer": "...",
    "sources": []
  },
  "status": "ok",
  "error": null,
  "request_id": "...",
  "latency_ms": 291
}
Por eso ahora Android debe leer:
response.data.answer
en lugar de:
response.answer

Se procede a 
1.crear en com.oraculo.app.data.remote.dto
AskDataDto

2.Modificar AskResponse.kt

3.Modificar OraculoRepository.kt
ahora falla, porque  intenta leer:
body.answer
body.sources

y ahora debe leer:
body.data?.answer
body.data?.sources

Problema luego de proceder a implementar el nuevo formato del backen da este error en la mayoria de preguntas: Detalle HTTP 500 en   /ask
la API cloud nueva probablemente espera un body más completo que solo:
{
  "question": "¿Qué es la CPU?"
}

Según el contrato de pruebas actual del backend, el body recomendado es:
{
  "question": "¿Qué es la CPU?",
  "language": "es",
  "top_k": 3
}

el contrato de respuesta actual es:
{
  "status": "ok",
  "error": null,
  "data": {
    "answer": "texto de la respuesta",
    "sources": [
      {
        "source": "Unidad 3.txt",
        "label": "Unidad 3",
        "version": "new"
      }
    ]
  },
  "request_id": "uuid",
  "latency_ms": 399
}

Solucion:

Se modifica askRequest.tk
pasa de:

data class AskRequest(
    val question: String,
    val user_id: String? = null,
    val context: List<String> = emptyList()
)

a ser

data class AskRequest(
    val question: String,
    val language: String = "es",
    val top_k: Int = 3
)

y dentro de:
OraculoRepository.kt
pasa de:
AskRequest(
    question = question,
    user_id = "android-test",
    context = emptyList()
)

a ser:

AskRequest(
    question = question,
    language = "es",
    top_k = 3
)

de igual forma se pasa a modificar
OraculoRepositore.kt adaptado al contrato de respuestas que se utiliza a partir de la v0.5 del API IA
Bloque modificado:
suspend fun ask(question: String): Result<String>

se procede como toque final a ajustar AskDataDto ya que el backend ahora también manda request_id y latency_ms dentro de data
agregando el siguiente codigo:

 val request_id: String? = null,
    val latency_ms: Int? = null

