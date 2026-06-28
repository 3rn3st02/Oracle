# Troubleshooting — ORACLE Android v1.6.4

## 1. Contexto técnico de la versión

La v1.6.4 añade personalización basada en el nombre del usuario sin cambiar el modelo persistente de Room ni el contrato backend. El cambio principal consiste en crear una nueva capa visual efímera dentro del RecyclerView usando un tipo específico:

```kotlin
ChatUiItem.WelcomeMessage
```

El objetivo era mostrar un mensaje de bienvenida personalizado al iniciar la app o al crear un nuevo chat, manteniéndolo fuera del historial persistido.

Esta decisión introduce nueva complejidad en tres puntos delicados:

1. `ChatUiItem.kt` — nuevo tipo visual.
2. `ChatAdapter.kt` — nuevo view type y nueva identidad visual.
3. `MainActivity.kt` — mezcla de mensajes efímeros con mensajes reales de Room.

---

## 2. Incidencias reales detectadas durante implementación

### 2.1. Error: `Unresolved reference 'OracleWelcomeMessageProvider'`

#### Síntoma

Android Studio marcó errores de referencia no resuelta en `MainActivity.kt` al usar:

```kotlin
OracleWelcomeMessageProvider
```

#### Causa

Faltaba el import del nuevo provider creado en:

```text
com.oraculo.app.ui.chat.OracleWelcomeMessageProvider
```

#### Solución

Añadir en `MainActivity.kt`:

```kotlin
import com.oraculo.app.ui.chat.OracleWelcomeMessageProvider
```

#### Comprobación

La importación desaparece del listado de errores y el compilador reconoce el objeto provider.

---

### 2.2. Error: `Unresolved reference 'ChatUiItem'`

#### Síntoma

Android Studio marcó errores sobre:

```kotlin
ChatUiItem
ChatUiItem.WelcomeMessage
mutableListOf<ChatUiItem>()
```

#### Causa

Faltaba importar el modelo visual:

```text
com.oraculo.app.ui.chat.models.ChatUiItem
```

#### Solución

Añadir en `MainActivity.kt`:

```kotlin
import com.oraculo.app.ui.chat.models.ChatUiItem
```

#### Comprobación

Kotlin vuelve a inferir correctamente los tipos del `mutableListOf<ChatUiItem>()` y de las funciones que devuelven `ChatUiItem.WelcomeMessage?`.

---

### 2.3. Error: `Cannot infer type for type parameter` / `let` con tipo no inferible

#### Síntoma

Aparecieron errores de inferencia de tipo en bloques como:

```kotlin
buildEphemeralWelcomeUiItem(conversationId)?.let { welcomeItem ->
    uiItems.add(welcomeItem)
}
```

#### Causa

Era un error en cascada debido a imports ausentes (`ChatUiItem` y `OracleWelcomeMessageProvider`).

#### Solución

Dos soluciones complementarias:

1. Corregir imports.
2. Simplificar el bloque para mayor robustez:

```kotlin
val welcomeItem = buildEphemeralWelcomeUiItem(conversationId)
if (welcomeItem != null) {
    uiItems.add(welcomeItem)
}
```

#### Resultado

Se reduce la dependencia de inferencia automática en un punto donde Android Studio ya venía contaminado por otros errores previos.

---

### 2.4. Error: `Conflicting declarations: local val uiItems` 

#### Síntoma

Dentro de `observeConversationInRecycler(...)` aparecieron errores de doble declaración de:

```kotlin
val uiItems = ...
```

#### Causa

Se añadió el nuevo bloque de bienvenida encima del bloque anterior sin reemplazarlo completamente. Como resultado coexistían dos variables con el mismo nombre en el mismo scope.

Ejemplo del problema:

```kotlin
val uiItems = ChatUiMapper.mapMessagesToUiItems(messages)
...
val uiItems = mutableListOf<ChatUiItem>()
```

#### Solución

Separar dos conceptos distintos:

- `mappedItems` → mensajes reales de Room.
- `finalUiItems` → lista final visible que mezcla bienvenida efímera + mensajes reales.

Estructura correcta:

```kotlin
val mappedItems = ChatUiMapper.mapMessagesToUiItems(messages)
val finalUiItems = mutableListOf<ChatUiItem>()

val welcomeItem = buildEphemeralWelcomeUiItem(conversationId)
if (welcomeItem != null) {
    finalUiItems.add(welcomeItem)
}

finalUiItems.addAll(mappedItems)
chatAdapter.submitItems(finalUiItems)
```

---

### 2.5. Fallo grave: el chat activo dejó de mostrar mensajes USER y ASSISTANT

#### Síntoma

Después de introducir la bienvenida efímera, el historial seguía mostrándose correctamente, pero el chat activo dejó de pintar mensajes nuevos del usuario y del asistente.

#### Diagnóstico

Esto fue la pista decisiva:

- **Historial sí funciona** → `ChatUiMapper` y `ChatAdapter` no estaban rotos globalmente.
- **Chat activo no funciona** → el fallo estaba en la construcción de la lista visual del chat activo.

#### Causa raíz

La función `observeConversationInRecycler(...)` había quedado parcheada con bloques superpuestos, variables duplicadas y mezcla incorrecta entre la lista base y la lista final visual.

En otras palabras:

- `mappedItems` y `uiItems` se estaban gestionando de forma inconsistente;
- parte de la lógica previa seguía viva;
- `chatAdapter.submitItems(...)` no recibía siempre una lista coherente.

#### Solución aplicada

Se reemplazó **completa** la función:

```kotlin
private fun observeConversationInRecycler(conversationId: String)
```

por una versión limpia con estas fases bien separadas:

1. cancelar observador anterior;
2. marcar conversación activa;
3. observar mensajes de Room;
4. mapear mensajes persistidos a `mappedItems`;
5. crear `finalUiItems`;
6. insertar bienvenida efímera si existe;
7. añadir mensajes reales;
8. enviar lista final al adapter;
9. mantener auto-scroll.

#### Lección técnica

Cuando se introduce una nueva capa visual efímera en un flujo ya existente, es más seguro reemplazar función completa que insertar parches locales sobre lógica previa.

---

### 2.6. El historial no debe mostrar la bienvenida efímera

#### Riesgo

Al introducir `WelcomeMessage`, existía el riesgo de que también apareciese en `openConversationReadOnly(...)` o en el mapeo del historial.

#### Protección aplicada

`buildEphemeralWelcomeUiItem(...)` devuelve `null` si:

```kotlin
isDisplayingReadOnlyConversation == true
```

Y además el historial usa directamente:

```kotlin
val uiItems = ChatUiMapper.mapMessagesToUiItems(messages)
```

sin mezclar el mensaje efímero.

#### Resultado

- Conversación activa → sí muestra bienvenida efímera.
- Historial → no muestra bienvenida efímera.

---

### 2.7. Riesgo de identidad visual inestable en `ChatAdapter.kt`

#### Síntoma potencial

Aunque el código compilase, el adapter podía interpretar el `WelcomeMessage` como una estructura distinta en cada refresco si no reconocía su identidad visual.

#### Causa

El adapter no usaba `DiffUtil`, pero sí una comparación manual:

```kotlin
private fun hasSameVisualIdentity(oldItem: ChatUiItem, newItem: ChatUiItem): Boolean
```

En esa función faltaba inicialmente el caso:

```kotlin
ChatUiItem.WelcomeMessage
```

#### Solución

Agregar:

```kotlin
oldItem is ChatUiItem.WelcomeMessage &&
newItem is ChatUiItem.WelcomeMessage -> {
    oldItem.id == newItem.id
}
```

#### Resultado

La bienvenida se considera un item estable dentro del RecyclerView y no fuerza invalidaciones estructurales innecesarias.

---

## 3. Validaciones específicas recomendadas tras cambios

### 3.1. Compilación

```bash
cd /home/ortzadar/Oracle/android/app-oraculo
./gradlew assembleDebug
```

### 3.2. Comprobar provider

Archivo esperado:

```text
android/app-oraculo/app/src/main/java/com/oraculo/app/ui/chat/OracleWelcomeMessageProvider.kt
```

Validar:

- package correcto;
- funciones públicas accesibles;
- listas no vacías;
- placeholder `{usuario}` correctamente reemplazado.

### 3.3. Comprobar imports en MainActivity

Deben existir:

```kotlin
import com.oraculo.app.ui.chat.OracleWelcomeMessageProvider
import com.oraculo.app.ui.chat.models.ChatUiItem
```

### 3.4. Comprobar estructura de `observeConversationInRecycler(...)`

Debe diferenciar claramente:

```kotlin
val mappedItems = ...
val finalUiItems = ...
```

No debe haber dos `val uiItems = ...` en el mismo scope.

### 3.5. Comprobar `ChatAdapter`

Debe soportar:

- `VIEW_TYPE_WELCOME_MESSAGE`
- `getItemViewType(...)`
- `onCreateViewHolder(...)`
- `onBindViewHolder(...)`
- `WelcomeMessageViewHolder`
- `hasSameVisualIdentity(...)` con `WelcomeMessage`

---

## 4. Puntos de diseño confirmados

### 4.1. La bienvenida es efímera

No debe persistirse en Room.

### 4.2. El historial sigue limpio

El historial solo muestra mensajes reales.

### 4.3. Los mensajes inferiores mantienen el mismo estilo visual

No se sustituyó el mecanismo de visualización de avisos; solo se personalizó el contenido textual.

### 4.4. El onboarding deja de ser decorativo

El nombre ya tiene impacto real en:

- bienvenida de chat;
- avisos inferiors;
- título del drawer.

---

## 5. Estado final del troubleshooting

La v1.6.4 introduce una mejora conceptual sencilla para el usuario, pero con una implementación delicada en la capa visual. Los fallos reales se concentraron en imports, duplicación de variables y mezcla incorrecta de listas dentro de `observeConversationInRecycler(...)`.

La solución robusta fue:

- crear el provider centralizado;
- añadir `WelcomeMessage` como tipo explícito;
- enseñar al adapter a tratarlo como item estable;
- reconstruir por completo `observeConversationInRecycler(...)` con lógica clara entre mensajes de Room y mensajes efímeros.

Con esto, la personalización queda funcional sin romper historial, scroll ni persistencia.
