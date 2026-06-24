# ORACLE – Pruebas v1.5.1

## Objetivo de la versión
La versión **v1.5.1** se centró en ampliar el árbol de navegación del panel lateral y mejorar su organización semántica mediante un nodo raíz general llamado **Temarios**.
ademas se fuerza el retraso de la respuesta del front end para dar un efecto mas natural a las AI que actualmente se utilizan
---

## Cambios implementados

### 1. Encapsulado general en "Temarios"
Se añadió un nodo raíz **Temarios** que encapsula todas las unidades del panel lateral:

- Unidad 1
- Unidad 2
- Unidad 3
- Unidad 4
- Unidad 5
- Unidad 6
- Unidad 7

El objetivo de este cambio fue hacer más clara la estructura global del contenido y dar una entrada única al conjunto completo de temas.

---

### 2. Integración de las Unidades 2 a 7
Se añadieron al panel lateral las siguientes unidades:

- Unidad 2
- Unidad 3
- Unidad 4
- Unidad 5
- Unidad 6
- Unidad 7

La construcción de cada unidad se realizó respetando el contenido del índice general del proyecto y la jerarquía interna de numerales, convirtiéndolos en nodos y subnodos del árbol. 【1-7d6d8e】

---

### 3. Jerarquía fiel al índice
Cada unidad se adaptó al formato de desplegable siguiendo esta lógica:

- numeral principal → nodo padre
- subnumeral → hijo
- subsubnumeral → nieto

Esto permitió que el árbol reflejara de forma coherente la estructura del temario original. 【1-7d6d8e】

---

### 4. Corrección del render del árbol
Durante la implementación de **Temarios**, la app compilaba correctamente pero el drawer aparecía vacío.

#### Causa detectada
La función `setupPromptDrawer()` no estaba siendo llamada dentro de `onCreate()`.

#### Solución aplicada
Se volvió a invocar explícitamente `setupPromptDrawer()` en la inicialización de la pantalla, restaurando el render del árbol completo.

#### Resultado
El panel lateral volvió a mostrar correctamente:

- nodo raíz `Temarios`
- unidades 1 a 7
- temas y subtemas asociados

---

## Comportamiento esperado validado

### Nodo raíz
- `Temarios` se expande/contrae
- `Temarios` no se envía como prompt
- `Temarios` actúa solo como contenedor

### Nodos con hijos
- click → expandir / contraer
- doble click → enviar exactamente el título del nodo

### Nodos hoja
- click → enviar exactamente el título visible

---
### 5. Retraso de respuesta forzado
Se añade dentro del codigo la siguiente linea 
kotlinx.coroutines.delay(25) 
Dentro del bloque:
val token = chunk.token
 
if (!token.isNullOrEmpty()) {
    onToken(token)
 }
dentro de data.repository, para dar un efecto de escritura mas natural al momento de que el frontend este recibiendo la respuesta de forma activa.


---

## Estado final

Esta versión deja implementado:

-Temarios como nodo raíz
-las Unidades 2 a 7 dentro del desplegable
-el árbol de navegación ajustado al índice general del proyecto, que ya incluye esas unidades en secuencia.
- Navegación lateral ahora permite recorrer el temario completo desde un único contenedor raíz, manteniendo la lógica del envío de prompts y la coherencia jerárquica del índice.
