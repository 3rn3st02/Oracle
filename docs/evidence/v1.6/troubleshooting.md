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

