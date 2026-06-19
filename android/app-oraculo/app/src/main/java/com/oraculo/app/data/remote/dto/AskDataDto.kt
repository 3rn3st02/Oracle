package com.oraculo.app.data.remote.dto

data class AskDataDto(
    val answer: String?,
    val sources: List<SourceDto> = emptyList(),
    val request_id: String? = null,
    val latency_ms: Int? = null,

)