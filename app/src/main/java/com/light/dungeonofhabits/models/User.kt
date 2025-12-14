package com.light.dungeonofhabits.models

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("_id")
    val id: String,
    val username: String,
    val email: String,
    val level: Int,
    val atk: Int,
    val def: Int,
    val hp: Int,
    val maxHp: Int,
    val xp: Int,
    val gold: Int,
    val inventory: List<InventoryItem>,
    val lastLogin: String,
    val tasks: List<Task>,
    val selectedCharacter: String?,
    val selectedPet: Pet?
)
