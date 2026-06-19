#1.4.2 Compatibilidad visual con navigation bar - 

##problema a solventar:
Bug visual con navigation bar (botones Android inferiores)

---
##Propuesta:
UX real + compatibilidad multiplataforma (gestos vs botones)

---

## Causa

El layout manejaba correctamente el teclado, pero no estaba respetando completamente la altura de la barra de navegación inferior del sistema.

El problema afectaba principalmente al componente:

- composerContainer
- editQuestion
- buttonAsk

---
Meta:

La barra de envío (composerContainer) debe:
- respetar el teclado
- respetar la barra de navegación
- adaptarse a interfaz (gestos y botones)

Identificacion del problema:
val keyboardHeight = max(0, imeInsets.bottom - systemBars.bottom)

---
## Solucion:
Modificar MainActivity.kt y modificar linea codigo, especificamente la linea de problema identificada en el punto anterior, por bloque de codigo completo adaptativo a interfaz con navigation bar y gestos

actualizar movimiento del composerContainer

---
## Errores de compilación
MainActivity.kt:
Call requires API level 31 (current min is 30): `android.content.ContextWrapper#getParams`
Unresolved reference 'bottomMargin'.
Assignment type mismatch: actual type is 'ContextParams?', but 'ViewGroup.LayoutParams!' was expected.
Call requires API level 31 (current min is 30): `android.content.ContextWrapper#getParams`
Call requires API level 31 (current min is 30): `android.content.ContextWrapper#getParams`

---
## Problema:
viene de una variable mal resuelta en MainActivity.kt.
Android está interpretando algo como params como si fuera ContextWrapper.getParams, que requiere API 31. Eso pasa cuando Kotlin no está tomando los LayoutParams de la vista, sino otra referencia llamada params.

---
## Solución al error de compilacion:
sar un nombre más explícito y obtener los márgenes directamente por lo que se procede a modificar todo el bloque private fun setupInsets() de MainActivity.kt; Usando explícitamente: composerView.layoutParams as ViewGroup.MarginLayoutParams

Esto nos deja una fluidez y compatibilidad completa entre:
- altura del teclado
- altura de la barra de navegación inferior
- dispositivos con gestos
- dispositivos con botones inferiores

---

## Conclusión

La versión V1.4.2 corrige un detalle importante de compatibilidad visual y mejora la experiencia de uso en diferentes configuraciones de navegación Android.
