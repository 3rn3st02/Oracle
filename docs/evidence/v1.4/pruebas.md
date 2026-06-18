Durante la mejora de la interfaz tipo chat, se modificó la zona inferior de escritura para que la caja de texto funcionara de forma más parecida a una barra de mensajes moderna.
El objetivo era:

mantener el fondo animado intacto
ubicar la caja de texto en la parte inferior
evitar que el teclado tapara el campo de escritura
permitir que el campo creciera según el tamaño del texto
mantener los mismos IDs para no romper la lógica de MainActivity


#1. Problema: la caja de texto quedaba tapada por el teclado
Error observado
Al abrir el teclado, la caja de texto inferior quedaba anclada abajo y el usuario no podía ver correctamente lo que estaba escribiendo.
##Causa
El layout no estaba reaccionando correctamente al teclado porque:

la barra inferior estaba anclada al fondo del ConstraintLayout
el teclado cubría parte de la interfaz
no se estaba usando correctamente el inset del IME (WindowInsetsCompat.Type.ime())
el padding se estaba aplicando con cuidado al contentContainer, pero todavía faltaba mover el contenedor inferior de escritura

##Solución aplicada
Se agregó manejo de insets para detectar la altura del teclado y mover dinámicamente el contenedor inferior composerContainer.
También se mantuvo la decisión correcta de no aplicar padding al root main, para que el fondo animado siguiera ocupando toda la pantalla.
Resultado
La caja de texto ahora sube sobre el teclado y permite visualizar la escritura correctamente.

#2. Problema: riesgo de romper el fondo animado
Error potencial
Al modificar el manejo de insets, existía riesgo de que el fondo animado se recortara o dejara de ocupar toda la pantalla.
Causa
Si se aplicaba padding directamente al layout raíz main, también se afectaba el fondo animado NightChatBackgroundView.
Solución aplicada
Se mantuvo la estrategia correcta:

el root main no recibe padding
el fondo animado sigue ocupando toda la pantalla
el padding se aplica solamente al contentContainer
la caja inferior se mueve mediante bottomMargin, no mediante padding del root

##Resultado
El fondo animado se mantiene intacto y el contenido respeta barras del sistema, teclado y navegación.

3. Problema: errores de compilación en setupInsets()
Errores observados
Durante la modificación de setupInsets(), aparecieron errores como:
Unresolved reference 'view'
Cannot infer type for type parameter 'T'
Unresolved reference 'setPadding'

##Causa
El método quedó mezclado con referencias incompletas o variables fuera de contexto.
Los problemas principales fueron:

uso de una variable view que no existía en ese bloque
findViewById sin tipado explícito en algunos casos
Kotlin no podía inferir correctamente el tipo de algunas vistas
contentContainer no estaba siendo reconocido como View

##Solución aplicada
Se reordenó el bloque de insets usando referencias explícitas a las vistas.
Se aseguró que las vistas principales estuvieran declaradas y enlazadas correctamente:

private lateinit var contentContainer: View
private lateinit var composerContainer: View

y que fueran inicializadas en bindViews():
contentContainer = findViewById(R.id.contentContainer)
composerContainer = findViewById(R.id.composerContainer)

También se corrigió el método setupInsets() para evitar referencias inválidas.
Resultado
La app volvió a compilar correctamente y el movimiento del input funciona sin afectar la animación de fondo.

#4. Problema: setupListeners no reconocido

##Error observado:
Unresolved reference 'setupListeners'

##Causa
Durante los cambios, la función setupListeners() quedó eliminada, mal ubicada o fuera de la clase MainActivity.
Solución aplicada
Se restauró el método setupListeners() dentro de MainActivity, manteniendo:

envío con botón buttonAsk
envío desde teclado cuando aplica IME_ACTION_SEND

##Resultado
El botón de enviar volvió a funcionar correctamente y la app mantuvo el flujo actual de preguntas.

#5. Problema: BuildConfig no reconocido

##Error observado:
Unresolved reference 'BuildConfig'

##Causa
BuildConfig no estaba siendo resuelto correctamente dentro de MainActivity, posiblemente por imports o por cómo quedó organizado el archivo tras los cambios.

##Solución aplicada
Se evitó depender directamente de BuildConfig en MainActivity y se usó la configuración centralizada:
NetworkConfig.BASE_URL

Esto mantiene la lectura de URL en un solo lugar y reduce errores en MainActivity.
Resultado
El log de URL volvió a funcionar correctamente y MainActivity quedó más limpia.

#6. Problema: el campo de texto no crecía con el contenido

##Problema observado
La caja de escritura funcionaba, pero no se adaptaba bien cuando el usuario escribía un texto más largo.
Causa
El EditText tenía una altura demasiado rígida o no estaba configurado para crecer correctamente.

##Solución aplicada
Se ajustó el bloque EditText para usar:

layout_height="wrap_content"
minLines="1"
maxLines="10"
gravity="top|start"
scrollbars="vertical"
inputType="textCapSentences|textMultiLine"

##Resultado
La caja de texto ahora puede crecer con el contenido hasta un límite razonable, sin romper el layout.

#7. Problema: warnings de accesibilidad en el input y botón
##Warnings observados:
Touch target size too small

##Afectaba a:
editQuestion
buttonAsk

##Causa
El campo de texto y el botón tenían un área táctil inferior a la recomendada por Android.
Android recomienda un área mínima aproximada de:
48dp


Se ajustaron alturas mínimas:

EditText con minHeight="48dp"
AppCompatButton con layout_height="50"
AppCompatButton con minHeight="48dp"
composerContainer con minHeight="64dp"

##Resultado
La barra inferior quedó más cómoda de usar y alineada con buenas prácticas de accesibilidad.

#8. Problema: Missing classes en Preview

##Warning observado
Missing classes

##Causa probable
Android Studio Preview podía tener problemas al renderizar la vista personalizada:
NightChatBackgroundView

Esto suele pasar cuando el Preview no puede instanciar correctamente clases custom, aunque la app compile y funcione.

##Solución aplicada
Se verificó que:

la clase existiera en la ruta correcta
el package fuera correcto
el XML apuntara a la ruta completa de la clase
la app compilara correctamente en dispositivo real
Desaparecio a sincronizar y al hacer build limpio

##Resultado
El warning no bloqueó el desarrollo. La app compiló y funcionó correctamente en el dispositivo.

#9. Resultado final de estos cambios
Después de los ajustes:

la app compila correctamente
el fondo animado se mantiene funcionando
la caja de texto está en la parte inferior
la caja sube cuando aparece el teclado
el texto escrito se puede visualizar correctamente
el input crece según el contenido
el botón conserva funcionalidad
la UI se acerca más a una experiencia tipo chat


##Conclusión
Los errores de esta fase estuvieron relacionados principalmente con:

manejo del teclado
insets del sistema
tipado de vistas en Kotlin
accesibilidad táctil
preservación del fondo animado

La solución final permitió mejorar la experiencia de escritura sin romper la arquitectura actual ni la integración con la API.
