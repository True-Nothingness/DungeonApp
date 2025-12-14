package com.light.dungeonofhabits.models

import java.util.Date

data class TaskRequest(
    val title: String,
    val difficulty: String,
    val type: String,
    val deadline: Date? = null
)
