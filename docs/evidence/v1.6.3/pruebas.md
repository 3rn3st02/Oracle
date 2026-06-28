# Pruebas — ORACLE Android v1.6.3

## Objetivo

Validar la versión ORACLE Android v1.6.3, centrada en sustituir el indicador circular de carga por una animación Lottie integrada visualmente sobre el input de consultas.

## Cambios bajo prueba

- Nueva Lottie de carga usando `panel_inferior_loading.json`.
- La animación se muestra sobre el input box.
- La animación se ajusta al tamaño del composer sin alterar su altura.
- La animación se activa con `setLoading(true)` y se oculta con `setLoading(false)`.
- El `ProgressBar` circular queda oculto como legacy.
- No se modifican backend, Room, streaming, feedback, historial ni scroll.

## Casos de prueba

### 1. Compilación

```bash
cd /Oracle/android/app-oraculo
./gradlew assembleDebug
````

Resultado esperado:

* Compila correctamente.
* No hay errores XML.
* El asset `panel_inferior_loading.json` se encuentra correctamente.

### 2. Activación de carga

Pasos:

1. Abrir la app.
2. Enviar una consulta.
3. Observar el input box durante la carga.

Resultado esperado:

* La animación Lottie aparece sobre el input.
* La animación no cambia el tamaño del input.
* El input mantiene su forma según el texto visible.

### 3. Finalización de carga

Pasos:

1. Esperar a que finalice la respuesta.
2. Observar el input.

Resultado esperado:

* La animación se detiene.
* La animación se oculta.
* El botón y el input vuelven a habilitarse.

### 4. No regresión

Validar que siguen funcionando:

* Envío de preguntas.
* Streaming.
* Scroll corregido de v1.6.1.
* Burbujas glass mate de v1.6.2.
* Copiar respuesta.
* Like/dislike.
* Historial.

## Evidencias

Las capturas se guardan en:

```text
docs/evidence/v1.6.3/
```

