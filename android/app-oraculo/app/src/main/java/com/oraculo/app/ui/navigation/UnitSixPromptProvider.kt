package com.oraculo.app.ui.navigation

object UnitSixPromptProvider {

    fun getUnitSix(): PromptNode {
        return PromptNode(
            title = "Unidad 6",
            children = listOf(

                /*
                 * 1. Periféricos de una computadora
                 */
                PromptNode(
                    title = "Periféricos de una computadora",
                    children = listOf(
                        PromptNode("Concepto de periférico "),                              // 1.1
                        PromptNode("Como interactúa un periférico con el computador"),      // 1.2
                        PromptNode("Que hace un periférico de una computadora"),            // 1.3
                        PromptNode("De que se encarga los periféricos de una computadora"), // 1.4
                        PromptNode("Elementos que forman los periféricos")                  // 1.5
                    )
                ),

                /*
                 * 2. Clasificación de los periféricos
                 */
                PromptNode(
                    title = "Clasificación de los periféricos",
                    children = listOf(

                        /*
                         * 2.1. Periféricos de entrada
                         */
                        PromptNode(
                            title = "Periféricos de entrada",
                            children = listOf(
                                PromptNode("Que hace el usuario con los periféricos de entrada"),      // 2.1.1
                                PromptNode("Ejemplos de periféricos de entrada (solo los más ")        // 2.1.2
                            )
                        ),

                        /*
                         * 2.2. Periféricos de salida
                         */
                        PromptNode(
                            title = "Periféricos de salida",
                            children = listOf(
                                PromptNode("Que hace el usuario con los periféricos de salida"), // 2.2.1
                                PromptNode("Ejemplos de periféricos de salida")                   // 2.2.2
                            )
                        ),

                        /*
                         * 2.3. Periféricos de Entrada y Salida (E/S)
                         */
                        PromptNode(
                            title = "Periféricos de Entrada y Salida (E/S)",
                            children = listOf(
                                PromptNode("Que son los periféricos de E/S") // 2.3.1
                            )
                        ),

                        /*
                         * 2.4. Periféricos de E/S de comunicaciones
                         */
                        PromptNode(
                            title = "Periféricos de E/S de comunicaciones ",
                            children = listOf(
                                PromptNode("Que hacen los periféricos de E/S de comunicaciones "), // 2.4.1
                                PromptNode("Ejemplos de periféricos de E/S de comunicaciones ")   // 2.4.2
                            )
                        ),

                        /*
                         * 2.5. Periféricos de Entrada y Salida (E/S) de almacenamiento
                         */
                        PromptNode(
                            title = "Periféricos de Entrada y Salida (E/S) de almacenamiento ",
                            children = listOf(
                                PromptNode("Que hacen los periféricos de E/S de almacenamiento "), // 2.5.1
                                PromptNode("Ejemplos de periféricos de E/S de almacenamiento ")   // 2.5.2
                            )
                        ),

                        /*
                         * 2.6. Periféricos de entrada y Salida (E/S) Multifunción
                         */
                        PromptNode(
                            title = "Periféricos de entrada y Salida (E/S) Multifunción",
                            children = listOf(
                                PromptNode("Que hacen los periféricos de E/S de Multifunción "), // 2.6.1
                                PromptNode("Ejemplos de periféricos de E/S multifunción ")       // 2.6.2
                            )
                        )
                    )
                )
            )
        )
    }
}