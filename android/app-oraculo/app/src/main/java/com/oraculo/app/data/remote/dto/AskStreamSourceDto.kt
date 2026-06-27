package com.oraculo.app.data.remote.dto

import com.google.gson.annotations.SerializedName

/*
 * Fuente devuelta por el evento final de /ask/stream.
 *
 * Ejemplo real observado:
 * {
 *   "source": "Pruebadoc.txt",
 *   "label": "Unidad 2: Unidades funcionales de un ordenador",
 *   "version": "1.0"
 * }
 */
data class AskStreamSourceDto(
    @SerializedName("source")
    val source: String? = null,

    @SerializedName("label")
    val label: String? = null,

    @SerializedName("version")
    val version: String? = null
)