# ORACLE – Troubleshooting v1.6.0

## 1. Error: `android:hintTextColor not found`
### Síntoma
El proyecto falló al compilar `view_onboarding_oracle.xml`.

### Causa
Se utilizó un atributo XML inválido:

```xml
android:hintTextColor
Solución
Sustituir por:
android:textColorHint
2. Problema: el árbol Temarios dejó de desplegar
Síntoma
Al tocar nodos del panel lateral, el drawer se cerraba o no desplegaba correctamente.
Causa
El onboarding se añadió como hijo directo del DrawerLayout, alterando el comportamiento del panel lateral.
Solución
Mover el onboarding dentro del ConstraintLayout principal, como overlay del contenido y no como tercer hijo directo del DrawerLayout.

3. Problema: el botón del drawer aparecía por encima del onboarding
Síntoma
El botón de apertura del panel lateral seguía visible o accesible durante el onboarding.
Causa
El contenedor del botón tenía elevation alta y el onboarding no lo tapaba completamente.
Solución
Se aplicaron dos medidas:

elevar el onboarding con android:elevation="64dp"
ocultar explícitamente buttonOpenDrawer y buttonOpenDrawerAura mientras el onboarding está visible


4. Problema: duplicidad de KAPT en Gradle
Síntoma
El archivo build.gradle.kts tenía doble declaración relacionada con kapt.
Causa
Existían a la vez:
id("org.jetbrains.kotlin.kapt")
id("kotlin-kapt")
Solución
Mantener solo:
id("kotlin-kapt")
5. Error: Unresolved reference savedQuestionTextStateKey
Síntoma
Compilación fallida en MainActivity.kt.
Causa
Las claves usadas en onSaveInstanceState() y restauración visual no estaban declaradas a nivel de clase.
Solución
Declararlas como variables de clase:

savedQuestionTextStateKey
savedAnswerTextStateKey
savedInputTextStateKey
savedLoadingStateKey


6. Problema: al rotar el móvil se perdía la respuesta visible
Síntoma
La respuesta mostrada desaparecía al recrearse la Activity.
Causa
El contenido visual no se estaba guardando en onSaveInstanceState().
Solución
Guardar y restaurar temporalmente:

textQuestion
textAnswer
editQuestion
estado básico de carga
session_id temporal


7. Problema: si rotaba durante streaming, la respuesta se cortaba
Síntoma
La respuesta se quedaba congelada en la última palabra recibida antes de la rotación.
Causa
La coroutine del stream corría en lifecycleScope, y al recrearse la Activity por rotación el lifecycleScope se cancelaba.
Solución
Añadir en AndroidManifest.xml:
android:configChanges="orientation|screenSize|keyboardHidden"

De esta forma, la Activity no se destruye al rotar y el stream continúa.

8. Problema: la animación del ojo no se veía bien
Síntoma
El ojo Lottie era negro y sobre fondo negro se perdía visualmente.
Causa
Contraste insuficiente del onboarding.
Solución
Añadir un Lottie de fondo claro/animado:

background_onboarding.json

y mantener el ojo Lottie encima.

9. Problema: el drawer dejó de funcionar tras añadir onboarding
Síntoma
El panel lateral parecía roto después de integrar el onboarding.
Causa real
No era la lógica del árbol, sino la jerarquía de vistas en activity_main.xml.
Solución
Reubicar correctamente el onboarding como overlay dentro del layout principal.

10. Estado de estabilidad
Tras aplicar las correcciones descritas:

el onboarding funciona
el panel lateral vuelve a desplegar
el stream sobrevive a rotación
Room compila
la base local queda inicializada

## 11. Problema: el feedback se sincronizaba pero perdía su estado visual
### Síntoma
Después de pulsar 👍 o 👎, el backend respondía OK, pero la UI dejaba de mostrar claramente el voto aplicado.

### Causa
Al marcar el feedback como sincronizado, se reemplazaba el estado visual por `SYNCED`, perdiendo la distinción entre `LIKE` y `DISLIKE`.

### Solución
Conservar `LIKE` o `DISLIKE` en `feedbackState` y usar `feedbackSynced=true` solo como marca interna de sincronización.

---

## 12. Problema: los botones 👍 / 👎 eran editables también en historial
### Síntoma
Las conversaciones abiertas en modo lectura permitían modificar la votación.

### Solución
Se añadió un modo `readOnly` al `ChatAdapter`:
- conversación activa -> copiar + 👍 + 👎 funcionales
- historial -> copiar funcional, 👍 / 👎 visibles pero bloqueados

---

## 13. Problema: `/health` no reflejaba bien el backend real
### Síntoma
Android mostraba `service: null`, `version: null`, `initialized: false`.

### Causa
El DTO `HealthResponse` no estaba alineado con la respuesta real actual del backend, que devuelve datos anidados en `data`.

### Solución
Adaptar `HealthResponse` al contrato real:
- status
- error
- data.service
- data.docs_count
- data.rag_has_content
- data.groq_configured
- data.cache_size
- data.questions_today
- data.questions_total

---

## 14. Problema: las sources del stream no coincidían con el modelo local inicial
### Síntoma
El backend devolvía sources con:
- source
- label
- version

pero el modelo local inicial esperaba otra estructura.

### Solución
Adaptar DTO, modelo local y entidad Room al contrato real del stream final.
