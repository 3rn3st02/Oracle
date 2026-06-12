package com.oraculo.app.data.remote.dto

data class HealthResponse(
    val status: String,
    val service: String,
    val version: String,
    val initialized: Boolean
)