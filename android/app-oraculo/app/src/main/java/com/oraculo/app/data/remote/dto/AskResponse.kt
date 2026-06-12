package com.oraculo.app.data.remote.dto

data class AskResponse(
    val answer: String?,
    val sources: List<String>,
    val status: String,
    val error: Any?,
    val request_id: String,
    val latency_ms: Int
)