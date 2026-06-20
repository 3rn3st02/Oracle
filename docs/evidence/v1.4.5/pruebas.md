# Pruebas V1.4.5 - Frases dinámicas en input inferior

## Objetivo

Validar la implementación de frases dinámicas aleatorias en la caja de texto inferior de ORACLE.

Esta mejora busca reforzar la identidad visual y narrativa de la app, mostrando una frase distinta como placeholder cada vez que se inicia la aplicación.

---

## Cambio principal

Antes la caja inferior de texto mostraba siempre un placeholder fijo.

Ahora, al iniciar la app, el campo de entrada muestra una frase aleatoria desde un proveedor dedicado.

---

## Frases incorporadas

- "No mires mis ojos, mira el espacio que hay entre nosotros. Ahí es donde vive tu respuesta."
- "Has venido cargado de preguntas que pesan más que tus pasos. Suelta el equipaje, el fuego hablará por ti."
- "El destino no es un lugar al que vas, es el eco de lo que ya has hecho. Pasa, escuchemos el eco."
- "Bienvenido al único rincón del mundo donde el tiempo no tiene prisa por pasar."
- "Llegas justo a tiempo... o un siglo tarde, en el tejido del destino da exactamente igual."

---

## Cambios aplicados en Android

### 1. Nuevo proveedor de frases

Se creó:

```text
OracleHintProvider.kt
Ubicación:
com.oraculo.app.ui.chat
Este archivo centraliza las frases usadas como placeholder dinámico.

---
### 2. MainActivity.kt
Se agregó una función para configurar el hint dinámico:
setupDynamicInputHint()
Esta función asigna una frase aleatoria al campo:
editQuestion

---
Validación
Se validó que:

la app compila correctamente
la app inicia sin errores
la caja inferior muestra una frase como placeholder
el texto no se envía como pregunta
al escribir, el placeholder desaparece correctamente
la lógica de envío se mantiene intacta
el streaming SSE sigue funcionando correctamente
el fondo animado y la UI actual no se ven afectados

---
Resultado
La app mejora su experiencia inicial y refuerza el estilo narrativo de ORACLE sin afectar la lógica de backend ni el flujo de preguntas.

---
Conclusión
La versión V1.4.5 añade una mejora ligera de UX en el input inferior, preparando la app para una experiencia más inmersiva y conversacional.
