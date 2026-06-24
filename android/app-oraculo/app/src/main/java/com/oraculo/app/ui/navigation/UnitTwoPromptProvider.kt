package com.oraculo.app.ui.navigation
object UnitTwoPromptProvider {

    fun getUnitTwo(): PromptNode {
        return PromptNode(
            title = "Unidad 2",
            children = listOf(

                /*
                 * 1. Unidades funcionales de un ordenador
                 */
                PromptNode(
                    title = "Unidades funcionales de un ordenador",
                    children = listOf(

                        /*
                         * 1.1. La arquitectura de John Von Neumann
                         */
                        PromptNode(
                            title = "La arquitectura de John Von Neumann",
                            children = listOf(
                                PromptNode("Características de la arquitectura de Von Neumann"),     // 1.1.1
                                PromptNode("Componentes principales de la arquitectura de Von Neumann"), // 1.1.2
                                PromptNode("¿Como funciona la arquitectura de Von Neumann?"),      // 1.1.3
                                PromptNode("¿Por qué es importante la arquitectura de Von Neumann") // 1.1.4
                            )
                        ),

                        /*
                         * 1.2. ¿Qué es un ordenador?
                         */
                        PromptNode(
                            title = "¿Qué es un ordenador?",
                            children = listOf(
                                PromptNode("¿Qué puede hacer un ordenador?"),        // 1.2.1
                                PromptNode("¿Qué necesita un ordenador para funcionar?") // 1.2.2
                            )
                        ),

                        /*
                         * 1.3. Unidades Funcionales de un ordenador
                         */
                        PromptNode(
                            title = "Unidades Funcionales de un ordenador",
                            children = listOf(

                                /*
                                 * 1.3.1. CPU – Central Processing Unit
                                 */
                                PromptNode(
                                    title = "CPU – Central Processing Unit",
                                    children = listOf(
                                        PromptNode("Funciones del CPU"),              // 1.3.1.1
                                        PromptNode("Registros dentro del CPU"),       // 1.3.1.2
                                        PromptNode("Unidad de medida del CPU")        // 1.3.1.3
                                    )
                                ),

                                /*
                                 * 1.3.2. Unidad de Memoria
                                 */
                                PromptNode(
                                    title = "Unidad de Memoria",
                                    children = listOf(
                                        PromptNode("Tipos generales de Unidad de Memoria"),      // 1.3.2.1
                                        PromptNode("Unidad de medida de las Unidades de Memoria") // 1.3.2.2
                                    )
                                ),

                                /*
                                 * 1.3.3. Unidad de Entrada y Salida
                                 */
                                PromptNode("Unidad de Entrada y Salida")
                            )
                        ),

                        /*
                         * 1.4. Buses de comunicación
                         */
                        PromptNode(
                            title = "Buses de comunicación",
                            children = listOf(

                                /*
                                 * 1.4.1. Tipos de buses de comunicación
                                 */
                                PromptNode(
                                    title = "Tipos de buses de comunicación",
                                    children = listOf(
                                        PromptNode("Data Bus"),     // 1.4.1.1
                                        PromptNode("Address Bus"),  // 1.4.1.2
                                        PromptNode("Control Bus")   // 1.4.1.3
                                    )
                                )
                            )
                        )
                    )
                ),

                /*
                 * 2. Unidad de Memoria
                 */
                PromptNode(
                    title = "Unidad de Memoria",
                    children = listOf(

                        /*
                         * 2.1. Tipos de memoria
                         */
                        PromptNode(
                            title = "Tipos de memoria",
                            children = listOf(
                                PromptNode("Memoria Caché"), // 2.1.1
                                PromptNode("RAM"),           // 2.1.2
                                PromptNode("ROM")            // 2.1.3
                            )
                        ),

                        PromptNode("Jerarquía de las memorias") // 2.2
                    )
                ),

                /*
                 * 3. La Unidad Central de Proceso (CPU)
                 */
                PromptNode(
                    title = "La Unidad Central de Proceso (CPU)",
                    children = listOf(
                        PromptNode("Partes principales del CPU"), // 3.1

                        PromptNode(
                            title = "Unidad aritmético-lógica (UAL)",
                            children = listOf(
                                PromptNode("Que hace la UAL") // 3.2.1
                            )
                        ),

                        PromptNode(
                            title = "Unidad de Control",
                            children = listOf(
                                PromptNode("Que hace la Unidad de Control") // 3.3.1
                            )
                        )
                    )
                ),

                /*
                 * 4. Unidad de entrada y Salida
                 */
                PromptNode(
                    title = "Unidad de entrada y Salida",
                    children = listOf(

                        /*
                         * 4.1. Tipos de dispositivos de entrada y salida
                         */
                        PromptNode(
                            title = "Tipos de dispositivos de entrada y salida",
                            children = listOf(
                                PromptNode("Dispositivos de entrada"), // 4.1.1
                                PromptNode("Dispositivos de salida"),  // 4.1.2
                                PromptNode("Dispositivos mixtos")      // 4.1.3
                            )
                        ),

                        /*
                         * 4.2. Función de la unidad de entrada/salida
                         */
                        PromptNode(
                            title = "Función de la unidad de entrada/salida",
                            children = listOf(

                                /*
                                 * 4.2.1. Como intercambian la información I/O Dev
                                 */
                                PromptNode(
                                    title = "Como intercambian la información I/O Dev",
                                    children = listOf(

                                        /*
                                         * 4.2.1.1. Interfaz
                                         */
                                        PromptNode(
                                            title = "Interfaz",
                                            children = listOf(
                                                PromptNode("Tipo de interfaces físicas") // 4.2.1.1.1
                                            )
                                        ),

                                        /*
                                         * 4.2.1.2. Controlador
                                         */
                                        PromptNode(
                                            title = "Controlador",
                                            children = listOf(
                                                PromptNode("Controlador de Hardware"), // 4.2.1.2.1
                                                PromptNode("Controlador de Software")  // 4.2.1.2.2
                                            )
                                        )
                                    )
                                ),

                                PromptNode("Formas de intercambio de datos I/O Dev") // 4.2.2
                            )
                        )
                    )
                )
            )
        )
    }
}
