# Troubleshooting — ORACLE Android v1.6.3

## Contexto

La versión v1.6.3 cambia el indicador de carga visual. Se reemplaza el uso visible del `ProgressBar` circular por una animación Lottie adaptada al input box.

## Problemas detectados durante implementación

### 1. La Lottie agrandaba el input

#### Causa

La Lottie se había insertado dentro del `composerContainer` con `match_parent`, dentro de un contenedor `wrap_content`. Esto hacía que la animación participara en la medición del layout.

#### Solución

La Lottie se dejó como vista hermana del `composerContainer`, constriñéndola a sus bordes. Así se adapta al tamaño del input sin alterarlo.

### 2. La Lottie se veía apagada

#### Causa

La animación estaba dibujada detrás del fondo del composer.

#### Solución

La Lottie se declaró después del `composerContainer` y se elevó visualmente con `elevation` / `translationZ`, manteniendo `clickable=false` y `focusable=false`.

### 3. El ProgressBar anterior ya no debe mostrarse

#### Solución

`setLoading()` deja `progressBar.visibility = View.GONE` y controla la Lottie con `playAnimation()` y `cancelAnimation()`.

## Verificaciones

### Asset Lottie

Comprobar que existe:

```bash
ls -lh android/app-oraculo/app/src/main/assets/panel_inferior_loading.json
````

### Compilación

```bash
cd /Oracle/android/app-oraculo
./gradlew assembleDebug
```

### Referencias en MainActivity

Comprobar que existen:

```kotlin
private lateinit var lottieInputLoadingBar: LottieAnimationView
lottieInputLoadingBar = findViewById(R.id.lottieInputLoadingBar)
```

Y que `setLoading()` controla la visibilidad y reproducción de la Lottie.

## Estado final

* La animación se muestra sobre el input.
* La animación no altera el tamaño del input.
* La animación se activa y detiene correctamente con la carga.
* No hay cambios funcionales en backend, Room, feedback, historial ni scroll.

