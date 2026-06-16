
Proyecto Oraculo

Aplicación móvil Android desarrollada en Kotlin que se conecta a un backend de inteligencia artificial basado en RAG (Retrieval Augmented Generation), desarrollado en Python.

El sistema permite realizar preguntas desde la app y obtener respuestas generadas a partir de documentos reales procesados.

---

Estado Actual:

-APK  V1.3 - UI + IA funcional  
- Sistema completamente operativo end-to-end  
- Backend RAG integrado y probado
- API RAG probada v0.2.5 de forma local  
- Evolución hacia entorno cloud en progreso  

---

Arquitectura:

Android (Kotlin)  
↓  
API FastAPI (Python)  
↓  
RAG (Procesamiento de documentos)  
↓  
Modelo LLM (Groq - en integración)

---

Funcionalidades

- Envío de preguntas desde la app Android
- Respuestas basadas en documentos reales
- Procesamiento mediante RAG
- Conexión local (ADB reverse / WiFi)
- UI  con fondo animado estilo cielo nocturno
- Manejo de errores y estados del sistema

---

 Mejoras:
**Android:
 UI v1.3: 16062026:

- Fondo animado nativo con Canvas
- Nebulosa RGB, estrellas y meteoros
- Parallax y animación continua
- Botón principal estilo glass con efecto RGB
- Corrección de ProgressBar en Preview
- Optimización del ciclo de vida de animación


** Backend IA
 RAG v.0.2.5: 15062026

- Procesamiento de documentos en `data/books`
- Generación de contexto en `data/processed`
- Ingestión manual mediante script Python

 RAG v.0.2.6: 16062026
- Integración con LLM (Groq)

---

Estructura del proyecto:

android/
backend/
docs/
├── evidence/
│    ├── v1.2/
│    └── v1.3/
├── setup.md
├── troubleshooting.md

## Evidencias

Ejemplos de funcionamiento disponibles en:

docs/evidence/

Incluye:
- UI en ejecución
- Respuestas de la IA
- interacción completa

---

## Documentación

### Setup
Guía para levantar el proyecto:
docs/setup.md

### Troubleshooting
Errores encontrados y soluciones:
 docs/troubleshooting.md

### Pruebas por versión
Validaciones del sistema:
 docs/evidence/VX.X/pruebas.md

---

## ⚙️ Requisitos

- Android Studio
- Python 3
- ADB
- Git
- FastAPI

---

 Flujo de desarrollo

1. Desarrollo paralelo:
   - Android (frontend)
   - IA RAG (backend)

2. Integración:
   - conexión mediante API REST

3. Validación:
   - pruebas manuales
   - evidencias por versión

4. Iteración:
   - mejoras UI
   - mejoras IA

---

Próximos pasos
16062026: 

- Migración completa a backend en la nube
- Integración final con Groq API
- V1.4 → interfaz tipo chat (estilo ChatGPT)
  - historial de conversación
  - burbujas de mensajes
  - scroll dinámico

---

 Roles

### Ernesto
- Desarrollo Android
- Integración con backend
- Testing y validación
- Entorno de Programación: Arch Linux - CachyOS - Omarchy

### Imanol
- Desarrollo IA RAG
- API FastAPI
- Despliegue backend
- Entorno de Programación: Kali Linux

---

 Notas

- El proyecto evoluciona activamente
- A partir de la  v0.2.6  del backend requieren API keys (Groq)
- El entorno local se usa principalmente para validación

---

 Releases

Android-Client
Cada versión incluye:

- APK descargable
- evidencias visuales
- documentación asociada
