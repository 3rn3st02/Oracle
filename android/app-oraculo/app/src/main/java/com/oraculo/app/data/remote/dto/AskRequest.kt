package com.oraculo.app.data.remote.dto

data class AskRequest(
    val question: String,
    val user_id: String? = null,
    val context: List<String> = emptyList()
)