package com.oraculo.app.ui.navigation

object UnitSevenPromptProvider {

    fun getUnitSeven(): PromptNode {
        return PromptNode(
            title = "Unidad 7",
            children = listOf(

                /*
                 * 1. Preparación del montaje de componentes internos
                 */
                PromptNode(
                    title = "Preparación del montaje de componentes internos",
                    children = listOf(
                        PromptNode("Objetivo de la preparación para el montaje de componentes internos"), // 1.1
                        PromptNode("Materiales para el montaje de"),                                      // 1.2
                        PromptNode("Herramientas para el montaje")                                        // 1.3
                    )
                ),

                /*
                 * 2. Preparación de la caja
                 */
                PromptNode(
                    title = "Preparación de la caja",
                    children = listOf(
                        PromptNode("Instalación de la fuente de alimentación"),            // 2.1
                        PromptNode("Instalación del sistema de refrigeración en el chasis") // 2.2
                    )
                ),

                /*
                 * 3. Instalación del procesador en la placa base
                 */
                PromptNode(
                    title = "Instalación del procesador en la placa base",
                    children = listOf(
                        PromptNode("Instalación del sistema de refrigeración del procesador") // 3.1
                    )
                ),

                /*
                 * 4. Instalación de memoria RAM
                 */
                PromptNode("Instalación de memoria RAM"),

                /*
                 * 5. Instalación de la placa base
                 */
                PromptNode(
                    title = "Instalación de la placa base",
                    children = listOf(
                        PromptNode("Instalación del cableado") // 5.1
                    )
                ),

                /*
                 * 6. Instalación del disco duro y otras unidades de almacenamiento
                 */
                PromptNode("Instalación del disco duro y otras unidades de almacenamiento"),

                /*
                 * 7. Instalación de unidades ópticas
                 */
                PromptNode("Instalación de unidades ópticas"),

                /*
                 * 8. Instalación de tarjetas de expansión
                 */
                PromptNode("Instalación de tarjetas de expansión"),

                /*
                 * 9. Remate del montaje
                 */
                PromptNode(
                    title = "Remate del montaje",
                    children = listOf(
                        PromptNode("Colocación final del cableado"), // 9.1
                        PromptNode("Repaso de instalación")          // 9.2
                    )
                ),

                /*
                 * 10. Sustitución de componentes
                 */
                PromptNode(
                    title = "Sustitución de componentes",
                    children = listOf(
                        PromptNode("Sustitución de fuente de alimentación"),                         // 10.1
                        PromptNode("Sustitución del chasis"),                                        // 10.2
                        PromptNode("Sustitución del microprocesador"),                               // 10.3
                        PromptNode("Sustitución del sistema de refrigeración del microprocesador"),  // 10.4
                        PromptNode("Sustitución de la placa base"),                                  // 10.5
                        PromptNode("Sustitución del disco duro y unidades ópticas"),                 // 10.6
                        PromptNode("Sustitución de tarjetas de expansión")                           // 10.7
                    )
                ),

                /*
                 * 11. Errores comunes durante el montaje
                 */
                PromptNode("Errores comunes durante el montaje"),

                /*
                 * 12. Resumen breve para el montaje de una computadora de torre
                 */
                PromptNode("Resumen breve para el montaje de una computadora de torre")
            )
        )
    }
}
