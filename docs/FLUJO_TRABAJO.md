# Flujo de trabajo del proyecto ORACLE

Última actualización: 18 de junio de 2026

---

## 1. Objetivo del flujo

Mantener un trabajo ordenado entre el cliente Android y el backend IA RAG, evitando romper `main` y permitiendo validar cada versión antes de integrarla.

---

## 2. Ramas principales

### `main`

Rama estable del proyecto.

Reglas:

- no se trabaja directamente sobre `main`,
- solo recibe versiones validadas,
- debe compilar,
- debe representar un estado integrado Android + backend.

### `android-client`

Rama principal de Ernesto.

Se usa para:

- desarrollo Android,
- UI/UX,
- integración API,
- adaptación a contratos JSON,
- pruebas en dispositivo real,
- documentación de evidencias,
- generación de APKs y releases.

### `backend-rag`

Rama principal de Arandeitors.

Se usa para:

- backend FastAPI,
- RAG,
- ingestión,
- Docker,
- cloud,
- cache,
- feedback,
- cambios de contrato API.

Esta rama se trabaja localmente en worktree paralelo:

```text
/home/ortzadar/Oracle-backend-rag
```

---

## 3. Flujo de trabajo de Ernesto

### Desarrollo Android

1. Cambiar a `android-client`.
2. Implementar cambios.
3. Compilar localmente.
4. Probar en dispositivo real.
5. Crear evidencias en `docs/evidence/vX.X.X/`.
6. Crear o actualizar `pruebas.md`.
7. Hacer commit.
8. Hacer push a `origin/android-client`.
9. Crear tag APK.
10. Crear release con APK.
11. Cuando esté validado, merge a `main`.

---

## 4. Flujo de trabajo de Arandeitors

1. Trabajar en `backend-rag`.
2. Modificar backend, RAG o contrato API.
3. Probar localmente o en cloud.
4. Versionar backend.
5. Informar cambios de contrato a Android.
6. Mantener claridad sobre:
   - request esperado,
   - response esperado,
   - campos nuevos,
   - campos eliminados,
   - campos que Android debe ignorar.

---

## 5. Integración Android + backend

Cuando hay cambios en backend:

1. Arandeitors informa el cambio.
2. Ernesto valida con `curl`.
3. Ernesto adapta DTOs si hace falta.
4. Ernesto valida en Android real.
5. Se documenta el cambio.
6. Se crea release Android si corresponde.
7. Se integra a `main`.

---

## 6. Flujo actual para cambios de contrato JSON

Cuando backend cambia JSON:

1. Confirmar contrato con ejemplo real.
2. Probar endpoint con `curl`.
3. Revisar `AskRequest.kt`.
4. Revisar `AskResponse.kt`.
5. Revisar DTOs internos como `AskDataDto.kt`.
6. Revisar `OraculoRepository.kt`.
7. Compilar Android.
8. Probar en dispositivo real.
9. Documentar en `docs/evidence/vX.X.X/pruebas.md`.
10. Crear tag y release.

---

## 7. Flujo actual de releases APK

Cada release Android debe incluir:

- commit en `android-client`,
- push a `origin/android-client`,
- build exitoso con `./gradlew assembleDebug`,
- carpeta `releases/vX.X.X/`,
- tag anotado `APK_VX.X.X`,
- GitHub Release con APK,
- enlaces a documentación y evidencias.

Se recomienda no adjuntar imágenes duplicadas al release si ya están en `docs/evidence/`. En su lugar, el release debe incluir links al tag.

---

## 8. Flujo de merge a main

Cuando una versión está validada:

```bash
git checkout main
git pull origin main
git merge origin/android-client
git merge origin/backend-rag
./gradlew assembleDebug
git push origin main
```

Nota:

Si `backend-rag` está abierto en un worktree paralelo, no se debe hacer checkout local de esa rama dentro de `/home/ortzadar/Oracle`. Se debe usar:

```bash
git merge origin/backend-rag
```

---

## 9. Flujo posterior al merge

Después de actualizar `main`, se debe sincronizar `android-client`:

```bash
git checkout android-client
git merge main
git push origin android-client
```

Esto evita que `android-client` quede detrás de `main`.

---

## 10. Reglas de documentación

Cada versión importante debe tener:

```text
docs/evidence/vX.X.X/pruebas.md
```

Ese archivo debe incluir:

- objetivo,
- contexto,
- problema detectado,
- solución aplicada,
- validación,
- conclusión.

Documentos globales:

- `README.md`,
- `docs/setup.md`,
- `docs/troubleshooting.md`,
- `docs/INFORME_TECNICO_CRONOLOGICO.md`,
- `docs/ESTADO_ACTUAL.md`,
- `docs/FLUJO_TRABAJO.md`,
- `docs/CHANGELOG.md`.

---

## 11. Buenas prácticas actuales

- Validar primero con `curl`.
- Confirmar después desde Android real.
- No asumir que un `200 OK` implica respuesta útil.
- Revisar logs de OkHttp cuando cambie el backend.
- No duplicar imágenes en releases si ya están en `docs/evidence`.
- Mantener `main` como estable.
- No usar `push --force` salvo decisión explícita y coordinada.
- Evitar trabajar directamente en `main`.

---

## 12. Próxima fase sugerida

La siguiente fase recomendada es:

```text
V1.5 - Chat real con historial
```

Flujo esperado:

1. crear modelo local de mensajes,
2. crear lista de conversación,
3. renderizar burbujas usuario/IA,
4. mostrar estado de carga,
5. permitir uso de `related_question` como sugerencia interactiva.

---

## 13. Estado del flujo al 18 de junio de 2026

El flujo actual es estable.

`main` y `android-client` quedaron sincronizadas tras integrar Android y backend.

El proyecto está listo para continuar con nuevas funcionalidades desde `android-client`, manteniendo `main` como base estable.
