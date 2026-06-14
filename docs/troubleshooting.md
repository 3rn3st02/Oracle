
# Troubleshooting ORACLE

## Problema: respuestas vacías

Causa:
No existían datos en data/books

Solución:
- añadir PDFs
- ejecutar ingestión

---

## Error: pypdf no encontrado

Mensaje:
ModuleNotFoundError: pypdf

Solución:
pip install pypdf

---

## Error: conexión móvil

Problema:
No conecta a localhost

Solución:
adb reverse tcp:8000 tcp:8000

---

## Error: Gson parsing

Mensaje:
Expected a string but was BEGIN_OBJECT

Causa:
Cambio en API (sources como objeto)

Solución:
- Crear SourceDto.kt
- Actualizar AskResponse.kt

---

## Error: backend responde pero app falla

Causa:
DTO incorrecto

Solución:
Actualizar modelos Kotlin

---

## Problema: carpeta data no existe

Solución:
mkdir -p data/books
mkdir -p data/processed
