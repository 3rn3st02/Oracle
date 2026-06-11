# Flujo de trabajo ORACULO

## Reglas
1. No hacer push directo a `main`
2. Cada tarea debe ir en una rama independiente
3. Todo cambio entra por Pull Request
4. Todo cambio de API debe documentarse en `docs/api/`
5. No subir secretos, tokens, `.env` ni `local.properties`

## Ramas
- `main` → rama estable
- `feature/...` → nuevas funcionalidades
- `fix/...` → correcciones
- `docs/...` → documentación

## Ejemplos de ramas
- `feature/android-base`
- `feature/android-ui-chat`
- `feature/backend-fastapi-base`
- `feature/backend-endpoint-ask`
- `docs/contrato-api-v1`
