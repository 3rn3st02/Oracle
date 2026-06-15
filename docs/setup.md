
# Setup del Proyecto ORACLE

## Requisitos

- APK
- Python 3
- ADB
- Git
- Python-pip
- Python-virtualenv

---

## Backend (IA RAG)

1) Asegúrate de tener el repo actualizado
   cd ~/Oracle
   git fetch origin

2) Cambiar a la rama backend
   git checkout backend-rag

3)Ir al backend
    cd backend/ia-rag

4)Crear entorno Python (MUY IMPORTANTE)
    python -m venv .venv
    source .venv/bin/activate

5) Instalar dependencias
    pip install -r requirements.txt

Lo anterior resumido:
  -cd ~/Oracle-backend-rag/backend/ia-rag (o ejecutar la rama del back    end desde la ubicacion local de tu ordenador)
  -source .venv/bin/activate
  ## En otra terminal Levantar backend
  -uvicorn app.main:app --reload --host 0.0.0.0 --port 8000

## Conectar Android (ADB reverse)
   -adb reverse tcp:8000 tcp:8000
   -adb reverse --list
      (respuesta esperada: UsbFfs tcp:8000 tcp:8000)
