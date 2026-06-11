# ORACULO - Backend IA RAG

Backend base de ORACULO desarrollado con FastAPI, preparado para ser consumido por la app Android sin tocar la parte `android/` del monorepo.

## Objetivo

Proveer una API simple, estable y documentada para que la app Android pueda:

- comprobar el estado del backend
- enviar preguntas a la IA
- recibir respuestas JSON previsibles
- evolucionar más adelante a un sistema RAG real

## Estructura principal

```text
backend/ia-rag/
├── app/
│   ├── main.py
│   ├── api/
│   │   └── routes.py
│   ├── models/
│   │   ├── request_models.py
│   │   └── response_models.py
│   ├── services/
│   │   └── rag_service.py
│   ├── core/
│   │   └── config.py
├── tests/
├── requirements.txt
├── .env.example
└── README.md
