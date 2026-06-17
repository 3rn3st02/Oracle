# Pruebas V1.3.1 - Integración con API cloud v0.3.2

## Objetivo

Validar que la app Android consulta correctamente la API remota desplegada en:

https://api-oraculo.sus.pe/

Esta versión reemplaza el flujo principal de pruebas locales por consultas directas a la API en red.

---

## Cambios principales

- Se actualiza la app para consumir la API cloud
- Se deja de usar como flujo principal:
  - backend local con uvicorn
  - adb reverse
  - IP local
- Las pruebas pasan a realizarse directamente contra la API desplegada

---

## Validaciones realizadas

### 1. Conectividad con API remota

Resultado:
✔ La app consulta correctamente `https://api-oraculo.sus.pe/`

---

### 2. Método correcto

Resultado:
✔ La app utiliza `POST /ask`
✔ La API responde con status `ok`

---

### 3. Respuesta en pantalla

Resultado:
✔ La app muestra respuestas reales del backend
✔ Se mantiene funcionalidad completa con UI actual

### 4. Correccón de Versión

Resultado:

✔ La app muestra su version del estado actual y no de las anteriores


---

## Evidencia

Se adjuntan capturas de la app funcionando con la API remota.

---

## Conclusión

✔ Integración cloud exitosa
✔ La app ya no depende del flujo local para pruebas principales
✔ La versión 1.3.1 queda validada sobre backend remoto
