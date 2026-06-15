
# Setup del Proyecto ORACLE

## Requisitos

- Android Studio
- Python 3
- ADB
- Git

---

## Backend (IA RAG)

```bash
cd backend/ia-rag
python -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
pip install pypdf

## Levantar backend

vicorn app.main:app --host 0.0.0.0 --port 8000

## Conectar Android (ADB reverse)

adb reverse tcp:8000 tcp:8000
