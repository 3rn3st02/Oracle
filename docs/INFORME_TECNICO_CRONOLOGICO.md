
# Informe técnico cronológico - Proyecto ORACLE

## 1. Resumen ejecutivo

ORACLE es un sistema compuesto por:

- un cliente Android desarrollado en Kotlin
- un backend IA RAG desarrollado en Python con FastAPI
- una integración progresiva desde entorno local hasta API remota en la nube

El objetivo del proyecto ha sido construir una aplicación Android capaz de consultar una IA basada en documentos reales, inicialmente mediante backend local y posteriormente mediante despliegue remoto.

Este documento resume la evolución técnica del proyecto, los cambios más importantes por fase, la distribución actual de responsabilidades, la forma de trabajo por ramas y el estado actual del sistema.

---

## 2. Arquitectura general actual

Cliente Android (Kotlin)
↓
API FastAPI (Python)
↓
Sistema RAG
↓
Documentos reales procesados

Entorno actual de integración principal:
- API cloud: https://api-oraculo.sus.pe/

Entornos de trabajo:
- Ernesto: Android / integración / pruebas / documentación
- Arandeitors: backend IA / ingestión / despliegue

---

## 3. Línea de trabajo por ramas

### Ramas principales

- `main`
  - rama estable e integrada
  - representa el estado funcional consolidado del sistema

- `android-client`
  - rama de desarrollo del cliente Android
  - aquí se implementan UI, UX, integración, pruebas y documentación del lado Android

- `backend-rag`
  - rama de desarrollo del backend IA
  - aquí se desarrollan FastAPI, RAG, ingestión de documentos, scoring y despliegue

### Regla de trabajo

- no se trabaja directamente sobre `main`
- cada desarrollador trabaja en su rama
- las integraciones se validan antes de hacer merge
- `main` debe contener únicamente estados reproducibles y funcionales

---

## 4. Roles y responsabilidades actuales

### Ernesto

Responsabilidades principales:

- desarrollo del cliente Android
- diseño y evolución de UI/UX
- integración con la API
- validación funcional desde dispositivo real
- documentación de setup, troubleshooting, pruebas y evidencias
- empaquetado, tags, releases y APKs

### Arandeitors

Responsabilidades principales:

- desarrollo del backend FastAPI
- desarrollo del sistema RAG
- ingestión de documentos y transformación a texto
- evolución del scoring y de la lógica de recuperación
- despliegue cloud y configuración de entorno
- gestión de versiones del backend

---

## 5. Cronología técnica del proyecto

### Fase 1 - Base Android y estructura inicial
### Periodo aproximado: 10 de junio de 2026

Se creó la base del proyecto Android en Kotlin y se estabilizó la configuración general del cliente.

Cambios importantes:
- creación del proyecto Android base
- corrección de Gradle y manifiesto
- organización inicial de estructura por capas
- primeras versiones funcionales del APK

Resultado:
- cliente Android arrancando correctamente
- app base funcional

Versiones relacionadas:
- APK_V1.0

---

### Fase 2 - Capa de red e integración inicial con backend
### Periodo aproximado: 10 al 12 de junio de 2026

Se implementó la capa de red del cliente Android y se comenzó la integración con la API.

Cambios importantes:
- creación de DTOs:
  - AskRequest
  - AskResponse
  - HealthResponse
- creación de:
  - OraculoApi
  - NetworkConfig
  - RetrofitClient
  - OraculoRepository
- integración de Retrofit
- uso de `/health` y `/ask`
- corrección de permisos INTERNET y manifest
- corrección de crash por Activity y problemas de serialización JSON

Resultado:
- cliente capaz de consultar backend
- arquitectura de integración lista

Versiones relacionadas:
- APK_V1.1
- APK_V1.1.2

---

### Fase 3 - Integración real con RAG local
### Periodo aproximado: 12 al 14 de junio de 2026

Se detectó que el backend respondía sin conocimiento útil porque faltaban datos reales e ingestión.

Problemas detectados:
- backend sin documentos cargados
- `data/books` vacío
- `data/processed` sin contenido útil
- respuestas genéricas del tipo:
  "No he encontrado todavía suficiente información..."

Acciones realizadas:
- instalación de `pypdf`
- carga del PDF `Montaje_y_mantenimiento_de_equipos.pdf`
- ejecución manual de `ingestion_service`
- generación de texto procesado en `data/processed`
