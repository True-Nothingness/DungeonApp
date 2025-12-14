package com.light.dungeonofhabits.models

data class InventoryItem(
    val itemName: String,
    val type: String,
    val quantity: Int,
    val effect: String,
    val amount: Int,
    val iconResId: Int,
)

