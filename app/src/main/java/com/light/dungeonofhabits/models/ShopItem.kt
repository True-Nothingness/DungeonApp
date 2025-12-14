package com.light.dungeonofhabits.models

data class ShopItem(
    val name: String,
    val type: String,
    val price: Int,
    val effect: String,
    val amount: Int,
    val iconResId: Int
)
