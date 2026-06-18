package com.oraculo.app.data.remote.dto

data class AskResponse(
    val data: AskDataDto?,
    val status: String,
    val error: Any?,
    val request_id: String?,
    val latency_ms: Int?
)