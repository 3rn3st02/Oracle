# Pruebas — ORACLE Android v1.6.1

## Objetivo

Validar los cambios de **ORACLE Android v1.6.1** centrados en:

- corrección del scroll durante respuestas largas en streaming;
- mejora del formato visual del texto recibido;
- conservación del comportamiento de copiar, like y dislike;
- no regresión de funcionalidades estables de v1.6.0.

## Entorno de prueba

- Rama: `android-client`
- Versión objetivo: `v1.6.1`
- Backend cloud: `https://api-oraculo.sus.pe/`
- Build previsto para release: APK debug
- Evidencias visuales: `docs/evidence/v1.6.1/`

## Casos de prueba

### 1. Respuesta larga en streaming sin tocar pantalla

**Pasos**

1. Abrir la app.
2. Enviar una pregunta que genere una respuesta larga.
3. No tocar la pantalla durante el stream.

**Resultado esperado**

- El chat baja automáticamente mientras llega la respuesta.
- El último bloque visible acompaña el avance del stream.
- No se queda anclado al inicio de la respuesta.

**Estado**

- Validado funcionalmente.

## 2. Respuesta larga interrumpiendo el scroll manualmente

**Pasos**

1. Enviar una pregunta larga.
2. Esperar a que empiece la respuesta.
3. Subir manualmente para leer contenido anterior.
4. Bajar de nuevo cerca del final.

**Resultado esperado**

- Al subir, la app respeta la lectura manual.
- Al volver cerca del final, el seguimiento automático puede reengancharse.
- No se bloquea la interacción durante el stream.

**Estado**

- Validado tras limpieza de `MainActivity.kt`.

## 3. Separación visual de respuesta larga

**Pasos**

1. Enviar una pregunta con respuesta estructurada.
2. Observar títulos, subtítulos, listas y puntos.

**Resultado esperado**

- La respuesta no se muestra como un único bloque gigante.
- Los títulos y subtítulos se diferencian visualmente.
- Los puntos con `*` se convierten en viñetas limpias.
- Los elementos numerados se muestran en una línea coherente.

**Estado**

- Validado con capturas en `docs/evidence/v1.6.1/`.

## 4. Copiar respuesta dividida en bloques

**Pasos**

1. Generar una respuesta larga dividida en bloques.
2. Pulsar el botón copiar del último bloque.
3. Pegar el contenido en un campo de texto.

**Resultado esperado**

- Se copia la respuesta completa.
- No se copia solo el último bloque.
- El texto copiado no conserva marcadores visibles innecesarios como `**` para títulos.

**Estado**

- Validado.

## 5. Like / dislike en respuesta dividida

**Pasos**

1. Generar una respuesta larga.
2. Esperar a que termine.
3. Pulsar 👍 o 👎.

**Resultado esperado**

- El feedback se aplica a la respuesta completa.
- El estado visual se refleja en el último bloque.
- No se crea feedback por bloque intermedio.
- No se rompe la asociación con `request_id`.

**Estado**

- Validado.

## 6. Historial

**Pasos**

1. Abrir una conversación anterior desde el drawer.
2. Revisar respuestas divididas y fuentes.

**Resultado esperado**

- La conversación se abre en modo lectura.
- Copiar sigue disponible.
- Like/dislike se muestran bloqueados.
- Fuentes se mantienen solo en historial.

**Estado**

- Sin cambios funcionales intencionados respecto a v1.6.0.

## 7. No regresión de funciones base

Se revisa que sigan operativas:

- onboarding;
- identidad local;
- nuevo chat;
- conversaciones locales;
- drawer de temarios;
- feedback real;
- sources solo en historial;
- health backend;
- Room como persistencia local.

**Estado**

- Sin regresiones detectadas durante el cierre de v1.6.1.

## Evidencias

Las capturas de validación se guardan en:

```text
docs/evidence/v1.6.1/
```

Esta carpeta debe enlazarse desde el release de GitHub, pero no adjuntarse como asset. El único asset del release debe ser el APK.
