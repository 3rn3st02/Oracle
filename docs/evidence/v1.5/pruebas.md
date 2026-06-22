
# 📊 ORACLE v1.5 – Pruebas, mejoras y resultados

## 🎯 Objetivo
Añadir funcion de panel lateral y dar una experiencia visual (drawer) mediante:

- Fondo animado dinámico con Lottie
- Transiciones suaves entre fondos
- Eliminación de lag inicial
- Optimización del rendimiento
- Boton de despliegue 🔮
---

## Cambios implementados

### Implementacion de panel lateral
Se implementa un panel lateral el cual despliega listas por temarios las cuales funcionan como un pre promt para consultar a la IA, actualmente implementado unicamente todo el temario de la Unidad 1

### Implementacion de boton para despliegue de panel lateral
Se añade un boton con el aspecto "🔮" con un fondo dinamico para que llame la atencion y sea clicado para el despliegue del panel lateral, ya que en primera instancia se desplegaba con gestos, pero estos eran algo incomodos de usar y si no se conocia la funcion el panel podia pasar desapercibido, sin embargo, se dejo la funcion de despliegue del panel lateral por gestos y clicando "🔮"

### Drawer dinámico con fondos Lottie
Se implementó un sistema que rota automáticamente entre múltiples fondos animados cada vez que se abre el drawer.

**Fondos utilizados:**
- fondo1.json
- fondo2.json "Se quita por causar problemas de rendimiento"
- fondo3.json "Se quita por causar problemas de rendimiento"
- fondo4.json
- fondo5.json

---

### Precarga (preload) de Lotties

####  Precarga principal
```kotlin
listOf(
    drawerBackgrounds[0],
    drawerBackgrounds[1]
).forEach { fileName ->
    LottieCompositionFactory.fromAsset(this, fileName)
}

Resultado:

Eliminado el lag en la primera apertura del drawer
Fondo visible inmediatamente

Precarga diferida (background)
lottieDrawerBackground.post {
    drawerBackgrounds.drop(2).forEach { fileName ->
        LottieCompositionFactory.fromAsset(this, fileName)
    }
}

Resultado:

Carga progresiva sin bloquear UI
Mejor equilibrio entre rendimiento y memoria


Transición suave entre fondos
Mejora aplicada
Uso de .post {}
Problema detectado:

Salto visual al cambiar fondo
Fade no sincronizado con render de Lottie

Solución:

Esperar un frame antes del fade-in

Resultado:

Transición fluida real
Eliminación de flicker visual


Protección contra errores de Lottie
Se añadió:
try {
    setAnimation(...)
} catch (e: Exception)
Problema previo:
IllegalArgumentException: needs >= 2 number of colors

Resultado:

La app no crashea aunque un JSON falle
Logging en Logcat para debugging


🐞 Errores encontrados y solucionados
1.Unresolved reference LottieAnimationView
Causa:
Falta de import
Solución:
import com.airbnb.lottie.LottieAnimationView
2.Crash por JSON inválido
Causa:
Gradientes mal definidos en Lottie
Solución:

Try/catch en carga
Sustitución de Lottie problemático por animación nativa (aura)


3.Lag en primera apertura
Causa:
Lottie no estaba precargado
Solución:

Preload inicial en onCreate


4.Salto visual en fade
Causa:
Fade-in antes de que Lottie renderice
Solución:

Uso de .post {}

Impacto en rendimiento
Controlado mediante:

Precarga parcial (no total)
Ejecución diferida
Uso de cache interno de Lottie


Estado de la versión
Estable
Lista para release APK
UX optimizada
