# Troubleshooting — ORACLE Android v1.6.2

## Contexto

La versión **v1.6.2** es una mejora visual sobre la base estable de **v1.6.1**. El cambio se limita a las burbujas del chat, aplicando un estilo glass mate con más espacio interno para mejorar legibilidad.

## Problemas conocidos durante implementación

### 1. Error XML por declaración duplicada

#### Síntoma

Durante la compilación apareció un error similar a:

```text
El destino de la instrucción de procesamiento que coincide con "[xX][mM][lL]" no está permitido.
```

#### Causa

El drawable contenía una declaración XML duplicada o una declaración `<?xml ... ?>` en una posición no válida.

#### Solución

Se dejó el archivo empezando directamente por:

```xml
<layer-list xmlns:android="http://schemas.android.com/apk/res/android">
```

sin declaración XML previa.

---

### 2. El efecto glass no cubría toda la burbuja

#### Síntoma

El efecto glass parecía no abarcar todo el área del texto.

#### Causa

El padding estaba definido dentro de los drawables `shape` mientras se usaba `layer-list`. Esto podía hacer que las capas visuales no coincidieran de forma limpia con el área real del `TextView`.

#### Solución

Se movió el padding a los `TextView` en:

- `item_chat_user.xml`
- `item_chat_assistant.xml`

Y se quitaron paddings internos de los drawables.

---

### 3. Burbujas demasiado brillantes

#### Síntoma

El primer efecto glass era demasiado luminoso o con apariencia de neón.

#### Solución

Se redujo la intensidad del brillo superior y del borde para obtener un efecto mate:

- Menos blanco en los gradientes.
- Bordes menos intensos.
- Sombra más discreta.

---

### 4. Texto demasiado pegado a los bordes

#### Síntoma

El texto se veía muy justo dentro de la burbuja, especialmente en mensajes de una línea.

#### Solución

Se aumentó el padding del `TextView`:

```xml
android:paddingStart="18dp"
android:paddingTop="12dp"
android:paddingEnd="18dp"
android:paddingBottom="12dp"
android:minHeight="44dp"
```

Esto da más aire visual sin tocar lógica del chat.

## Verificaciones si algo falla

### Compilación

Ejecutar:

```bash
cd /home/ortzadar/Oracle/android/app-oraculo
./gradlew assembleDebug
```

### Revisar drawables

Comprobar que los archivos empiezan por `<layer-list>` y no contienen dos declaraciones XML:

```bash
grep -n "<?xml" app/src/main/res/drawable/bg_chat_user.xml
grep -n "<?xml" app/src/main/res/drawable/bg_chat_assistant.xml
```

### Revisar que no se toca lógica

Los cambios de v1.6.2 no deberían modificar:

- `MainActivity.kt`
- `ChatAdapter.kt`
- `ChatUiMapper.kt`
- `ChatUiItem.kt`

salvo que se haya hecho una corrección posterior no prevista.

## Estado final

- El efecto glass mate compila.
- Las burbujas tienen más espacio interno.
- La mejora visual no modifica comportamiento funcional.
- La carpeta local `/home/ortzadar/Oracle/releases/` sigue siendo staging local y no debe subirse al repo.
