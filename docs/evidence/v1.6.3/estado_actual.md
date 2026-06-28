# Estado actual — ORACLE Android v1.6.3

## Resumen

La versión ORACLE Android v1.6.3 introduce una mejora visual en el estado de carga del input de consultas.

El indicador circular anterior basado en `progress_oracle_static.xml` queda como elemento legacy oculto, y se incorpora una animación Lottie horizontal usando `panel_inferior_loading.json`.

## Alcance

Esta versión modifica la experiencia visual de carga.

No modifica:

- Backend.
- Endpoints.
- DTOs.
- Room.
- Streaming.
- Scroll.
- Feedback.
- Copiar respuesta.
- Historial.
- Sources.
- Drawer.
- Onboarding.

## Cambios principales

- Se añade `LottieAnimationView` para carga sobre el input.
- La Lottie se ajusta al tamaño del `composerContainer`.
- La animación no altera el alto del input.
- La animación se muestra por encima del input para evitar que quede apagada por el fondo.
- Se mantiene `progressBar` como referencia legacy oculta.
- `setLoading()` controla reproducción y cancelación de la animación.

## Archivos relevantes

- `android/app-oraculo/app/src/main/res/layout/activity_main.xml`
- `android/app-oraculo/app/src/main/assets/panel_inferior_loading.json`
- `android/app-oraculo/app/src/main/java/com/oraculo/app/MainActivity.kt`

## Release previsto

- Rama: `android-client`
- Commit: `V1.6.3 feat(android): lottie de carga sobre input`
- Tag: `APK_1.6.3`
- Release: `APK 1.6.3`
- APK: `ORACLE_v1.6.3.apk`
- Staging local: `/Oracle/releases/v1.6.3/`

## Estado

Listo para commit, build, tag y release.
