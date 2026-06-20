# Pruebas V1.4.4 - Respuestas progresivas con SSE

## Objetivo

Validar la implementación del endpoint `/ask/stream` para mostrar respuestas progresivas en la app Android ORACLE.

Esta versión cambia el comportamiento de la respuesta completa tradicional por una experiencia de escritura progresiva.

---

## Cambio principal

Antes la app consultaba:

POST /ask

y esperaba una respuesta completa para mostrarla de una sola vez.

Ahora la app consulta:

POST /ask/stream

y procesa fragmentos progresivos tipo SSE.

---

## Formato esperado del backend

El backend envía chunks con el siguiente formato:

```json
{
  "token": "...",
  "done": false
}
Cuando la respuesta termina, el backend envía:

{
  "done": true
---

## Cambios aplicados en Android

### 1. Nuevo DTO
Se agregó:
AskStreamChunkDto.kt

para representar cada fragmento del stream.

### 2. RetrofitClient
Se expone el cliente OkHttp para permitir lectura directa del stream.
También se usa Gson para parsear cada chunk recibido.

### 3. OraculoRepository.kt
Se agregó el método:
suspend fun askStream(
    question: String,
    onToken: suspend (String) -> Unit
): Result<Unit>
Este método:

llama a /ask/stream
lee la respuesta línea por línea
soporta formato SSE con data:
soporta JSON por línea
procesa token
finaliza cuando llega done: true


### 4. MainActivity.kt
Se actualizó el flujo de envío para usar askStream.
Ahora la app:

-muestra "La respuesta que buscas no te dejara en paz, solo te dara una responsabilidad mas grande." al iniciar la consulta
-limpia el input después de enviar
-recibe tokens progresivos
-actualiza la UI en tiempo real
-mantiene el estado de carga hasta terminar el stream
---


Validación funcional
Se validó que:

la app compila correctamente
la app consulta POST /ask/stream
la respuesta aparece progresivamente
el texto se actualiza token por token
el estado se muestra antes del primer token
la app sigue usando la API cloud
la UI no pierde el fondo animado ni el input inferior
---

Resultado
La app ofrece una experiencia más cercana a un asistente conversacional real, mostrando la respuesta de forma progresiva en lugar de esperar el texto completo.
---

Conclusión
La versión V1.4.4 incorpora streaming SSE para mejorar la percepción de velocidad y naturalidad en las respuestas de ORACLE.
Esta versión prepara la base para la siguiente evolución: chat real con historial de mensajes.
