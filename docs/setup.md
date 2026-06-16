# Setup ORACLE - Backend IA RAG + App Android

Este documento describe cómo levantar el sistema completo en entorno local. Esto funciona hasta la v0.2.5 del RAG

---

#  1. Requisitos

- Python 3.10+
- Android Studio
- ADB
- Git
- LibreOffice (para conversión de documentos)

---

#  2. Backend IA RAG (Python)

## Crear entorno virtual

cd backend/ia-rag

python -m venv .venv  
source .venv/bin/activate  

---

## Instalar dependencias

pip install -r requirements.txt  
pip install pypdf  

---

#  3. Cargar documentos

## Ubicación:

data/books

---

## TIPOS ACEPTADOS

Actualmente el sistema trabaja con:

- PDF 

---

## Si el documento es .docx

Convertir a PDF:

libreoffice --headless --convert-to pdf archivo.docx

Mover el PDF:

mv archivo.pdf data/books/

---

#  4. Ingestión de documentos

## Ejecutar desde Python

python

```python (se debe cambiar xxxxxx por el nombre real del PDF que se utilice)
from app.services.ingestion_service import ingestion_service
ingestion_service.ingest_pdf("xxxxxx.pdf")

Esperar unos minutos a que termine la conversion y luego:

exit()

Resultado esperado
Se genera:
data/processed/*.txt

# 5. Levantar backend
uvicorn app.main:app --host 0.0.0.0 --port 8000

# 6. Validar backend
en otra terminal:
curl http://127.0.0.1:8000/health

7. Conectar Android

Opción 1 — USB (ADB reverse)
Requiere cable USB
adb reverse tcp:8000 tcp:8000
Configuración Android
BASE_URL:
http://127.0.0.1:8000/

 Opción 2 — WiFi (recomendado)
Obtener IP del PC
ip route
Ejemplo:
192.168.0.42

Probar desde móvil
http://192.168.0.42:8000/health

Configurar en Android studio:
BASE_URL:
http://192.168.0.42:8000/

9. Flujo completo

Instalar entorno Python
Cargar PDFs en data/books
Ejecutar ingestión
Levantar backend
Conectar Android (USB o WiFi)
Probar preguntas en app

 10. Validación final
✔ /health responde OK
✔ /ask devuelve respuestas reales
✔ App muestra resultados correctamente

