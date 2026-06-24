package com.oraculo.app.ui.navigation

object UnitThreePromptProvider {

    fun getUnitThree(): PromptNode {
        return PromptNode(
            title = "Unidad 3",
            children = listOf(

                PromptNode(
                    title = "La Placa Base",
                    children = listOf(

                        PromptNode("Características de la Placa Base"),

                        PromptNode(
                            title = "Factor de forma de la MotherBoard",
                            children = listOf(
                                PromptNode("MotherBoard ATX"),
                                PromptNode("MotherBoard Micro ATX"),
                                PromptNode("MotherBoard Mini-ITX"),
                                PromptNode("MotherBoard BTX"),
                                PromptNode("MotherBoard E-ATX"),
                                PromptNode("MotherBoard Thin Mini-ITX"),
                                PromptNode("MotherBoard SSI-CEB / SSI-EEB")
                            )
                        ),

                        PromptNode("Formatos en uso y desuso actualmente de las Placas Madre")
                    )
                ),

                PromptNode(
                    title = "Estructura de la Placa Base",
                    children = listOf(

                        PromptNode(
                            title = "Socket",
                            children = listOf(
                                PromptNode("Socket Intel"),
                                PromptNode("Socket AMD"),
                                PromptNode("Diferencias socket Intel y AMD"),
                                PromptNode("¿Existen solo 2 tipos de socket?"),
                                PromptNode("Como identificar el modelo de procesador de un socket")
                            )
                        ),

                        PromptNode(
                            title = "Chipset",
                            children = listOf(
                                PromptNode("Puente Norte"),
                                PromptNode("Puente Sur"),
                                PromptNode("Chipset PCH"),
                                PromptNode("Tipo de chipset más utilizado actualmente")
                            )
                        ),

                        PromptNode(
                            title = "BIOS",
                            children = listOf(
                                PromptNode("¿Para qué sirve la pila de la BIOS?"),
                                PromptNode("Real-Time Clocl (RTC)"),
                                PromptNode("CMOS Settings / Firmware Settings"),
                                PromptNode("Cambios importantes de la BIOS actualmente"),

                                PromptNode(
                                    title = "Arquitectura de la BIOS",
                                    children = listOf(
                                        PromptNode("BIOS x86"),
                                        PromptNode("BIOS x64"),
                                        PromptNode("Arquitectura de BIOS utilizada actualmente"),
                                        PromptNode("Diferencias entre x86 y x64")
                                    )
                                ),

                                PromptNode("BIOS Legacy"),
                                PromptNode("BIOS UEFI"),
                                PromptNode("Diferencias Legacy / UEFI"),
                                PromptNode("Sistema POST")
                            )
                        ),

                        PromptNode(
                            title = "Zócalos de RAM",
                            children = listOf(
                                PromptNode("Zócalo DIMM"),
                                PromptNode("Zócalo SO-DIMM"),
                                PromptNode("Tecnología actual y descontinuada en RAM")
                            )
                        ),

                        PromptNode(
                            title = "Buses de expansión",
                            children = listOf(
                                PromptNode("Gama PCI"),
                                PromptNode("Slot AGP"),
                                PromptNode("Gama PCI Express"),
                                PromptNode("Tecnología actual y descontinuada en buses de expansión")
                            )
                        ),

                        PromptNode(
                            title = "Conectores",
                            children = listOf(
                                PromptNode("Conectores internos de la placa base"),
                                PromptNode("Conector de corriente"),
                                PromptNode("Conector IDE"),
                                PromptNode("Conector SATA"),

                                PromptNode(
                                    title = "Cabeceras",
                                    children = listOf(
                                        PromptNode("Cabecera de Configuración"),
                                        PromptNode("Cabecera de Expansión de Puertos"),
                                        PromptNode("Caberea de Panel Frontal"),
                                        PromptNode("Cabecera USB"),
                                        PromptNode("Cabecera FireWire"),
                                        PromptNode("Cabecera de Audio Frontal"),
                                        PromptNode("Cabecera de config. De BIOS"),
                                        PromptNode("Tecnología actual y descontinuada en cabeceras")
                                    )
                                )
                            )
                        ),

                        PromptNode(
                            title = "Pila de la Placa Base",
                            children = listOf(
                                PromptNode("¿Para qué sirve la pila de la BIOS?"),
                                PromptNode("¿Qué mantiene la pila de la BIOS?"),
                                PromptNode("¿Qué pasa si la pila de la BIOS se agota?"),
                                PromptNode("¿Qué información se pierde al descargar la pila de la BIOS?")
                            )
                        )
                    )
                )
            )
        )
    }
}
