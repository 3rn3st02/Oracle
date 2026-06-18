package com.oraculo.app.data.remote.dto

data class AskRequest(
    val question: String,
    val language: String = "es",
    val top_k: Int = 3
)