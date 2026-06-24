package com.oraculo.app.ui.navigation

object UnitFivePromptProvider {

    fun getUnitFive(): PromptNode {
        return PromptNode(
            title = "Unidad 5",
            children = listOf(

                /*
                 * 1. Conexiones de un ordenador
                 */
                PromptNode(
                    title = "Conexiones de un ordenador",
                    children = listOf(
                        PromptNode("Pines de una conexión"),      // 1.1
                        PromptNode("Contactos de una conexión"),  // 1.2
                        PromptNode("Formatos de conexón"),        // 1.3
                        PromptNode("Alargadores"),                // 1.4
                        PromptNode("Adaptadores"),                // 1.5
                        PromptNode("HUBS de conexión")            // 1.6
                    )
                ),

                /*
                 * 2. Tipos de conectores
                 */
                PromptNode(
                    title = "Tipos de conectores",
                    children = listOf(
                        PromptNode("Din y Mini-DIN"), // 2.1
                        PromptNode("D-Subminiatura"), // 2.2
                        PromptNode("USB"),            // 2.3
                        PromptNode("FireWire"),       // 2.4
                        PromptNode("DVI"),            // 2.5
                        PromptNode("HDMI"),           // 2.6
                        PromptNode("RCA"),            // 2.7
                        PromptNode("Jack"),           // 2.8
                        PromptNode("RJ")              // 2.9
                    )
                ),

                /*
                 * 3. Puerto USB
                 */
                PromptNode(
                    title = "Puerto USB",
                    children = listOf(
                        PromptNode("Tipos de conexion USB"),     // 3.1
                        PromptNode("Versiones de USB"),          // 3.2
                        PromptNode("Familias del USB"),          // 3.3
                        PromptNode("Estandares actuales de USB") // 3.4
                    )
                ),

                /*
                 * 4. Puertos en Serie y en Paralelo
                 */
                PromptNode(
                    title = "Puertos en Serie y en Paralelo",
                    children = listOf(
                        PromptNode("Puerto en Serie"),    // 4.1
                        PromptNode("Puerto en Paralelo")  // 4.2
                    )
                ),

                /*
                 * 5. Puerto PS/2
                 */
                PromptNode("Puerto PS/2"),

                /*
                 * 6. Puerto FireWire
                 */
                PromptNode("Puerto FireWire"),

                /*
                 * 7. Puertos para video
                 */
                PromptNode(
                    title = "Puertos para video",
                    children = listOf(
                        PromptNode("VGA"), // 7.1
                        PromptNode("DVI"), // 7.2

                        PromptNode(
                            title = "HDMI",
                            children = listOf(
                                PromptNode("Tipos de HDMI"),     // 7.3.1
                                PromptNode("Versiones de HDMI"), // 7.3.2
                                PromptNode("Estandar actual del HDMI")  // 7.3.3
                            )
                        ),

                        PromptNode("Display port"), // 7.4
                        PromptNode("RCA de Video"), // 7.5
                        PromptNode("S-Video")       // 7.6
                    )
                ),

                /*
                 * 8. Puertos de Audio
                 */
                PromptNode(
                    title = "Puertos de Audio",
                    children = listOf(

                        PromptNode(
                            title = "Puerto Jack",
                            children = listOf(
                                PromptNode("Tipos de Jack"),                     // 8.1.1
                                PromptNode("Código de colores del puerto Jack"), // 8.1.2
                                PromptNode("Sistema envolvente y digital")       // 8.1.3
                            )
                        ),

                        PromptNode("RCA de audio"), // 8.2
                        PromptNode("MIDI")          // 8.3
                    )
                ),

                /*
                 * 9. Puertos para comunicaciones cableadas
                 */
                PromptNode(
                    title = "Puertos para comunicaciones cableadas",
                    children = listOf(
                        PromptNode("RJ-11"),        // 9.1
                        PromptNode("RJ-45"),        // 9.2
                        PromptNode("BCN"),          // 9.3
                        PromptNode("Fibra óptica")  // 9.4
                    )
                ),

                /*
                 * 10. Conexiones inalámbricas
                 */
                PromptNode(
                    title = "Conexiones inalámbricas",
                    children = listOf(
                        PromptNode("Wifi"),       // 10.1
                        PromptNode("Bluetooth"),  // 10.2
                        PromptNode("Infrarrojo")  // 10.3
                    )
                ),

                /*
                 * 11. Conectores de alimentación
                 */
                PromptNode("Conectores de alimentación"),

                /*
                 * 12. Conectores de controladora de disco
                 */
                PromptNode(
                    title = "Conectores de controladora de disco",
                    children = listOf(
                        PromptNode("IDE"),  // 12.1
                        PromptNode("SATA"), // 12.2
                        PromptNode("SCSI"), // 12.3
                        PromptNode("SAS")   // 12.4
                    )
                ),

                /*
                 * 13. Panel lateral de la placa base
                 */
                PromptNode("Panel lateral de la placa base")
            )
        )
    }
}
