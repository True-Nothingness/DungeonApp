package com.light.dungeonofhabits.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.Date

data class Task(
    @SerializedName("_id")
    val id: String,
    val title: String,
    val difficulty: String,
    val type: String,
    val completed: Boolean,
    val deadline: Date? = null
): Serializable

