package com.oraculo.app.data.remote.dto

import com.google.gson.annotations.SerializedName

/*
 * DTO real de /health según contrato actualmente observado.
 *
 * Respuesta real:
 * {
 *   "status": "ok",
 *   "error": null,
 *   "data": {
 *     "service": "Oráculo",
 *     "docs_count": 8,
 *     "rag_has_content": true,
 *     "groq_configured": true,
 *     "cache_size": 2,
 *     "questions_today": 44,
 *     "questions_total": 333
 *   }
 * }
 */
data class HealthResponse(
    @SerializedName("status")
    val status: String,

    @SerializedName("error")
    val error: String?,

    @SerializedName("data")
    val data: HealthData?
)

data class HealthData(
    @SerializedName("service")
    val service: String?,

    @SerializedName("docs_count")
    val docsCount: Int?,

    @SerializedName("rag_has_content")
    val ragHasContent: Boolean?,

    @SerializedName("groq_configured")
    val groqConfigured: Boolean?,

    @SerializedName("cache_size")
    val cacheSize: Int?,

    @SerializedName("questions_today")
    val questionsToday: Int?,

    @SerializedName("questions_total")
    val questionsTotal: Int?
)