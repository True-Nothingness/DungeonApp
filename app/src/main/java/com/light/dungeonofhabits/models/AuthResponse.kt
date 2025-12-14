package com.light.dungeonofhabits.models

data class AuthResponse(
    val token: String,
    val user: User
)