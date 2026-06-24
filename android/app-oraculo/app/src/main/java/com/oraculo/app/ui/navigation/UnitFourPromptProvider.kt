package com.oraculo.app.ui.navigation

object UnitFourPromptProvider {

    fun getUnitFour(): PromptNode {
        return PromptNode(
            title = "Unidad 4",
            children = listOf(

                /*
                 * 1. Componentes internos de un ordenador
                 */
                PromptNode("Componentes internos de un ordenador"),

                /*
                 * 2. La caja del Ordenador
                 */
                PromptNode(
                    title = "La caja del Ordenador",
                    children = listOf(
                        PromptNode("Características de la caja del ordenador"), // 2.1
                        PromptNode("Material y rigidez de la caja"),            // 2.2
                        PromptNode("Ventilación de la caja"),                   // 2.3
                        PromptNode("Peso de la caja"),                          // 2.4
                        PromptNode("Bahías de expansión de la caja"),           // 2.5
                        PromptNode("Conexiones frontales de la caja")           // 2.6
                    )
                ),

                /*
                 * 3. Estructura y distribución de la caja del Ordenador
                 */
                PromptNode(
                    title = "Estructura y distribución de la caja del Ordenador",
                    children = listOf(
                        PromptNode("Laterales de la caja"),       // 3.1
                        PromptNode("Parte posterior de la caja"), // 3.2
                        PromptNode("Parte frontal de la caja")    // 3.3
                    )
                ),

                /*
                 * 4. Modelos de cajas del Ordenador
                 */
                PromptNode(
                    title = "Modelos de cajas del Ordenador",
                    children = listOf(
                        PromptNode("Caja Gran torre"),      // 4.1
                        PromptNode("Caja Torre"),           // 4.2
                        PromptNode("Caja Semitorre"),       // 4.3
                        PromptNode("Caja Minitorre"),       // 4.4
                        PromptNode("Caja Microtorre"),      // 4.5
                        PromptNode("Caja Slim"),            // 4.6
                        PromptNode("Caja Mino o barebone"), // 4.7
                        PromptNode("Caja Sobremesa")        // 4.8
                    )
                ),

                /*
                 * 5. La fuente de alimentación
                 */
                PromptNode(
                    title = "La fuente de alimentación",
                    children = listOf(

                        PromptNode(
                            title = "Modelo PSU ATX",
                            children = listOf(
                                PromptNode("Conexiones PSU ATX") // 5.1.1
                            )
                        ),

                        PromptNode(
                            title = "Modelo PSU Mini ATX",
                            children = listOf(
                                PromptNode("Conexiones PSU Mini ATX") // 5.2.1
                            )
                        ),

                        PromptNode(
                            title = "Modelo PSU Flex ATX",
                            children = listOf(
                                PromptNode("Conexiones PSU Flex ATX") // 5.3.1
                            )
                        ),

                        PromptNode(
                            title = "Modelo PSU Mini-ITX",
                            children = listOf(
                                PromptNode("Conexiones PSU Mini-ITX") // 5.4.1
                            )
                        ),

                        PromptNode("PSU SFX"),               // 5.5
                        PromptNode("PSU SFX-L"),             // 5.6
                        PromptNode("PSU TFX"),               // 5.7
                        PromptNode("Fuentes propietarias"),  // 5.8
                        PromptNode("Pico PSU o DC-DC")       // 5.9
                    )
                ),

                /*
                 * 6. Fuente de alimentación en Laptop o Portátil
                 */
                PromptNode(
                    title = "Fuente de alimentación en Laptop o Portátil",
                    children = listOf(

                        PromptNode(
                            title = "Que conforman el PSU de una Laptop o Portátil",
                            children = listOf(
                                PromptNode("Tipos de conectores del PSU de una Laptop o Portátil"), // 6.1.1
                                PromptNode("¿Por qué se utiliza de forma externa el PSU en un portátil?") // 6.1.2
                            )
                        ),

                        PromptNode(
                            title = "Batería en Laptop o equipo portátil",
                            children = listOf(
                                PromptNode("De que está compuesta la batería de un portátil"),       // 6.2.1
                                PromptNode("Capacidad de la batería de un portátil"),                // 6.2.2
                                PromptNode("Que especifica la etiqueta de la batería de un portátil") // 6.2.3
                            )
                        )
                    )
                ),

                /*
                 * 7. El Microprocesador o Procesador
                 */
                PromptNode(
                    title = "El Microprocesador o Procesador",
                    children = listOf(
                        PromptNode("¿Qué hace el Microprocesador?"),          // 7.1
                        PromptNode("¿Cómo funciona el Microprocesador?"),     // 7.2
                        PromptNode("¿Cómo es físicamente el Microprocesador?"), // 7.3
                        PromptNode("¿De qué está hecho el Microprocesador?"), // 7.4

                        PromptNode(
                            title = "Características más importantes del Microprocesador",
                            children = listOf(
                                PromptNode("Frecuencia del Microprocesador"),                    // 7.5.1
                                PromptNode("Arquitectura de 32 y 64 bits del Microprocesador"), // 7.5.2
                                PromptNode("Bus del sistema del Microprocesador"),              // 7.5.3
                                PromptNode("Memoria caché del Microprocesador"),                // 7.5.4
                                PromptNode("Nivel de integración del Microprocesador"),         // 7.5.5
                                PromptNode("Núcleos del procesador"),                           // 7.5.6
                                PromptNode("Hilos del procesador"),                             // 7.5.7
                                PromptNode("Consumo y calor del Procesador")                    // 7.5.8
                            )
                        ),

                        PromptNode(
                            title = "El Microprocesador de portátiles",
                            children = listOf(
                                PromptNode("Características más importantes del microprocesador de portátiles") // 7.6.1
                            )
                        ),

                        PromptNode(
                            title = "Sistema de refrigeración de la caja del ordenador",
                            children = listOf(
                                PromptNode("Elementos que componen en el sistema de refrigeración de la caja"), // 7.7.1
                                PromptNode("Objetivo del sistema de refrigeración de la caja"),                // 7.7.2
                                PromptNode("Refrigeración pasiva"),                                             // 7.7.3
                                PromptNode("Refrigeración activa"),                                             // 7.7.4

                                PromptNode(
                                    title = "Tipos de refrigeración para ordenadores",
                                    children = listOf(
                                        PromptNode("Refrigeración por aire"),              // 7.7.5.1
                                        PromptNode("Refrigeración liquida AIO"),           // 7.7.5.2
                                        PromptNode("Refrigeración liquida personalizada"), // 7.7.5.3
                                        PromptNode("Refrigeración pasiva"),                // 7.7.5.4
                                        PromptNode("Refrigeración hibrida"),               // 7.7.5.5
                                        PromptNode("Proceso más común de refrigeración")   // 7.7.5.6
                                    )
                                )
                            )
                        )
                    )
                ),

                /*
                 * 8. Tipos de RAM
                 */
                PromptNode(
                    title = "Tipos de RAM",
                    children = listOf(
                        PromptNode("RAM DINAMICA"),               // 8.1
                        PromptNode("RAM ESTATICA"),               // 8.2
                        PromptNode("¿Qué es GDDR?"),              // 8.3
                        PromptNode("¿Qué es SDR?"),               // 8.4
                        PromptNode("¿Qué es DDR?"),               // 8.5
                        PromptNode("Diferencias entre DDR y SDR") // 8.6
                    )
                ),

                /*
                 * 9. Dispositivos de Almacenamiento
                 */
                PromptNode(
                    title = "Dispositivos de Almacenamiento",
                    children = listOf(
                        PromptNode("Disco duro HDD"),         // 9.1
                        PromptNode("Disco solido SSD"),       // 9.2
                        PromptNode("Diferencias entre SSD y HDD") // 9.3
                    )
                ),

                /*
                 * 10. Unidad Óptica
                 */
                PromptNode(
                    title = "Unidad Óptica",
                    children = listOf(
                        PromptNode("CD"),       // 10.1
                        PromptNode("DVD"),      // 10.2
                        PromptNode("Blu-Ray")   // 10.3
                    )
                ),

                /*
                 * 11. Dispositivos Flash
                 */
                PromptNode(
                    title = "Dispositivos Flash",
                    children = listOf(
                        PromptNode("Componentes que utilizan dispositivos flash"),   // 11.1
                        PromptNode("Características de dispositivos flash"),          // 11.2
                        PromptNode("Cualidad de los dispositivos flash")             // 11.3
                    )
                ),

                /*
                 * 12. Tarjetas de expansión
                 */
                PromptNode(
                    title = "Tarjetas de expansión",
                    children = listOf(
                        PromptNode("Tarjeta gráfica"),                          // 12.1
                        PromptNode("Tarjeta de red"),                           // 12.2
                        PromptNode("Tarjeta de sonido"),                        // 12.3
                        PromptNode("Tarjeta de captura de imagen/TV"),          // 12.4
                        PromptNode("Tarjeta de expansión de puertos USB"),      // 12.5
                        PromptNode("Tarjeta de expansión de puertos USB"),      // 12.6
                        PromptNode("¿Qué otros tipos de tarjetas de expansión hay?") // 12.7
                    )
                )
            )
        )
    }
}
