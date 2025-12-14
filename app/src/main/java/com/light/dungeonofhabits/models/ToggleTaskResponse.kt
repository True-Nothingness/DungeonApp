package com.light.dungeonofhabits.models

data class ToggleTaskResponse(
    val message: String,
    val completed: Boolean,
    val currentGold: Int,
    val leveledUp: Boolean
)