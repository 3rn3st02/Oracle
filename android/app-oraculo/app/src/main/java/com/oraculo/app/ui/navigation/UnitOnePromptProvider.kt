
package com.oraculo.app.ui.navigation

/*
 * Proveedor de prompts para Unidad 1.
 *
 * Fuente base:
 * Indice General.docx
 *
 * Regla:
 * - No se muestra numeración.
 * - Los títulos visibles son los prompts que se enviarán.
 * - El prompt enviado debe ser exactamente el texto visible.
 */
object UnitOnePromptProvider {

    fun getUnitOne(): PromptNode {
        return PromptNode(
            title = "Unidad 1",
            children = listOf(

                PromptNode(
                    title = "Electricidad básica",
                    children = listOf(
                        PromptNode("Concepto de Electricidad"),
                        PromptNode("Diferencia Potencial"),
                        PromptNode("Corriente Electrica"),
                        PromptNode("El Átomo y su composición")
                    )
                ),

                PromptNode(
                    title = "Sentido de la corriente",
                    children = listOf(
                        PromptNode("Sentido convencional de la corriente"),
                        PromptNode("Movimiento de Electrones"),
                        PromptNode("Intensidad de la corriente"),
                        PromptNode("Corriente continua"),
                        PromptNode("Corriente alterna"),
                        PromptNode("Ejemplo de intensidad de la corriente")
                    )
                ),

                PromptNode(
                    title = "Resistencia Eléctrica",
                    children = listOf(
                        PromptNode("Materiales"),
                        PromptNode("Conductores"),
                        PromptNode("Aislantes"),
                        PromptNode("Semiconductores")
                    )
                ),

                PromptNode(
                    title = "Circuito Eléctrico",
                    children = listOf(
                        PromptNode("Elementos básicos de un circuito"),
                        PromptNode(
                            title = "Circuito en Serie",
                            children = listOf(
                                PromptNode("Propiedades del circuito en serie"),
                                PromptNode("Formulas del circuito en serie"),
                                PromptNode("Ejemplo de aplicación circuito en serie")
                            )
                        ),
                        PromptNode(
                            title = "Circuito en paralelo",
                            children = listOf(
                                PromptNode("Propiedades del circuito en paralelo"),
                                PromptNode("Formulas principales del circuito en paralelo"),
                                PromptNode("Ejemplo de aplicación circuito en paralelo")
                            )
                        )
                    )
                ),

                PromptNode(
                    title = "Ley de Ohm",
                    children = listOf(
                        PromptNode("Interpretación sencilla de la Ley de Ohm"),
                        PromptNode("Ejemplo de la Ley de Ohm")
                    )
                ),

                PromptNode(
                    title = "Pilas y Baterías",
                    children = listOf(
                        PromptNode("Pila"),
                        PromptNode("Batería"),
                        PromptNode("Diferencias entre pila y batería"),
                        PromptNode("Magnitudes importantes de pila y batería"),
                        PromptNode("Ejemplos de pila"),
                        PromptNode("Ejemplos de batería")
                    )
                ),

                PromptNode(
                    title = "Interruptores y Pulsadores",
                    children = listOf(
                        PromptNode("Interruptor"),
                        PromptNode("Pulsador"),
                        PromptNode(
                            title = "Tipos de Accionamiento de Interruptor y Pulsador",
                            children = listOf(
                                PromptNode("Acción momentánea"),
                                PromptNode("Acción de enclavamiento"),
                                PromptNode("Acción alternada")
                            )
                        )
                    )
                ),

                PromptNode(
                    title = "Fuentes de Alimentación",
                    children = listOf(
                        PromptNode("Que es una fuente de alimentación"),
                        PromptNode("Función básica de una fuente de alimentación"),
                        PromptNode(
                            title = "Tipos generales de fuente de alimentación",
                            children = listOf(
                                PromptNode("Adaptador externo"),
                                PromptNode("Fuente interna de PC"),
                                PromptNode("Fuente de Laboratorio")
                            )
                        ),
                        PromptNode(
                            title = "Conceptos básicos para leer una etiqueta de una fuente de alimentación",
                            children = listOf(
                                PromptNode("AC INPUT"),
                                PromptNode("DC OUTPUT"),
                                PromptNode("MAX COMBINED WATTAGE"),
                                PromptNode("Potencia Eléctrica")
                            )
                        ),
                        PromptNode(
                            title = "Ejemplos realistas de lectura de etiqueta",
                            children = listOf(
                                PromptNode("Etiqueta Cargador portátil, como se lee y potencia aprox. de salida"),
                                PromptNode("Etiqueta cargador USB-C moderno, como se interpreta"),
                                PromptNode("Etiqueta fuente ATX de un pc, como se interpreta y calculo")
                            )
                        ),
                        PromptNode("Que revisar al elegir una fuente"),
                        PromptNode("Protecciones habituales en una fuente de alimentación")
                    )
                ),

                PromptNode(
                    title = "Componentes electrónicos",
                    children = listOf(
                        PromptNode(
                            title = "Las resistencias",
                            children = listOf(
                                PromptNode("Función principal de una resistencia"),
                                PromptNode("Datos habituales de una resistencia"),
                                PromptNode("Ejemplo de resistencias"),
                                PromptNode(
                                    title = "Resistencias modelo SMD",
                                    children = listOf(
                                        PromptNode("Características de las resistencias SMD"),
                                        PromptNode("Encapsulados comunes de resistencias SMD"),
                                        PromptNode("Lectura de valor de resistencias SMD")
                                    )
                                )
                            )
                        ),
                        PromptNode(
                            title = "Potenciómetros",
                            children = listOf(
                                PromptNode("Función de un potenciómetro"),
                                PromptNode("Aplicación de un potenciómetro"),
                                PromptNode("Tipo de potenciómetros")
                            )
                        ),
                        PromptNode(
                            title = "Condensadores",
                            children = listOf(
                                PromptNode("Funciones de un condensador"),
                                PromptNode("Tipos de condensadores"),
                                PromptNode("Ejemplo de condensadores")
                            )
                        ),
                        PromptNode(
                            title = "Diodos",
                            children = listOf(
                                PromptNode("Funciones de los Diodos"),
                                PromptNode("Tipos de Diodos"),
                                PromptNode("Ejemplo de Diodos")
                            )
                        ),
                        PromptNode(
                            title = "LEDs",
                            children = listOf(
                                PromptNode("Función de un led"),
                                PromptNode("Tipos de LED"),
                                PromptNode("Ejemplo de Led")
                            )
                        ),
                        PromptNode(
                            title = "Transistores",
                            children = listOf(
                                PromptNode("Función de un transistor"),
                                PromptNode("Tipos de Transistores"),
                                PromptNode("Importancia de los transistores en informática")
                            )
                        )
                    )
                ),

                PromptNode(
                    title = "Aparatos de Medición",
                    children = listOf(
                        PromptNode(
                            title = "Voltímetro",
                            children = listOf(
                                PromptNode("Como se conecta el voltímetro"),
                                PromptNode("Ejemplo de uso del voltímetro")
                            )
                        ),
                        PromptNode(
                            title = "Amperímetro",
                            children = listOf(
                                PromptNode("Como se conecta el amperímetro"),
                                PromptNode("Ejemplo de uso del amperímetro")
                            )
                        ),
                        PromptNode(
                            title = "Ohmímetro",
                            children = listOf(
                                PromptNode("Como se utiliza el ohmímetro"),
                                PromptNode("Ejemplo de uso del ohmímetro")
                            )
                        ),
                        PromptNode(
                            title = "Multímetro",
                            children = listOf(
                                PromptNode("Que puede medir el multímetro"),
                                PromptNode("Ventajas del multímetro"),
                                PromptNode("Tipos de multímetro")
                            )
                        ),
                        PromptNode(
                            title = "Osciloscopio",
                            children = listOf(
                                PromptNode("Que muestra el osciloscopio"),
                                PromptNode("Importancia del osciloscopio"),
                                PromptNode("Uso típico del osciloscopio"),
                                PromptNode("Unidades del osciloscopio")
                            )
                        ),
                        PromptNode("Por que actualmente se utilizan más el Multímetro y Osciloscopio")
                    )
                ),

                PromptNode(
                    title = "Circuitos integrados, CHIPS y sus niveles",
                    children = listOf(
                        PromptNode("Porque son importantes los chips"),
                        PromptNode(
                            title = "Escalas o niveles de integración",
                            children = listOf(
                                PromptNode(
                                    title = "SSI",
                                    children = listOf(
                                        PromptNode("Características SSI"),
                                        PromptNode("Ejemplos SSI")
                                    )
                                ),
                                PromptNode(
                                    title = "MSI",
                                    children = listOf(
                                        PromptNode("Características MSI"),
                                        PromptNode("Ejemplos MSI")
                                    )
                                ),
                                PromptNode(
                                    title = "LSI",
                                    children = listOf(
                                        PromptNode("Características LSI"),
                                        PromptNode("Ejemplos LSI")
                                    )
                                ),
                                PromptNode(
                                    title = "VLSI",
                                    children = listOf(
                                        PromptNode("Características VLSI"),
                                        PromptNode("Ejemplos VLSI")
                                    )
                                ),
                                PromptNode(
                                    title = "ULSI",
                                    children = listOf(
                                        PromptNode("Características ULSI"),
                                        PromptNode("USO ULSI")
                                    )
                                ),
                                PromptNode(
                                    title = "GLSI",
                                    children = listOf(
                                        PromptNode("Características GLSI"),
                                        PromptNode("Contexto actual de los chips")
                                    )
                                )
                            )
                        ),
                        PromptNode("Resumen de la evolución de los chips")
                    )
                )
            )
        )
    }
}
